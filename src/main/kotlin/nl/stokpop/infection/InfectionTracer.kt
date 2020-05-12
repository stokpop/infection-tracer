package nl.stokpop.infection

import com.ryanharter.ktor.moshi.moshi
import java.lang.RuntimeException
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.system.exitProcess
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.*
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import nl.stokpop.infection.event.*

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Provide one of the following:")
        println("text <number of agents> <demo cycles> <number of meetings per cycle>")
        println("ui <port>")
        exitProcess(1)
    }

    if (args.isNotEmpty()) {
        val command = args[0]
        when (command) {
            "ui" -> executeUserInterface(args)
            "text" -> executeTextInterface(args)
        }
    }

}

fun executeUserInterface(args: Array<String>) {
    val port = determinePort(args)
    println("Running UI on port $port")

    val server = embeddedServer(Netty, port = port) {
        install(StatusPages) {
            exception<Throwable> { e ->
                println(e)
                call.respondText(e.localizedMessage,
                    ContentType.Text.Plain, HttpStatusCode.InternalServerError)
            }
        }
        install(WebSockets)
        install(ContentNegotiation) {
            moshi {
                add(LocalDateTime::class.java, LocalDateTimeAdapter())
            }
        }
        routing {
            get("/") {
                val message = MeetEvent(AgentInfo("agent-1", ContactState.SAFE), AgentInfo("agent-2", ContactState.SAFE), LocalDateTime.now())
                call.respond(message)
            }
            get("/demo") {
                call.respond(ContactHash())
            }
            webSocket("/session") { // websocketSession
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText().trim()
                            if (text.equals("bye", ignoreCase = true)) {
                                close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                            }
                            val demo = InfectionTracer().demo(InfectionTracerRun(4, 4, 4))
                            demo.forEach { e -> Thread.sleep(500); outgoing.send(Frame.Text(e.toString())); }
                            //outgoing.send(Frame.Text(Encounter(LocalDateTime.now(), ContactHash(), ContactHash()).toString()))
                        }
                        else -> outgoing.send(Frame.Text("Unsupported frame: ${frame.frameType}"))
                    }
                }
            }
        }
    }
    server.start(wait = true)
}

private fun determinePort(args: Array<String>): Int {
    val port: Int
    if (args.size == 2) {
        port = args[1].toInt()
    } else {
        port = 8181
    }
    return port
}

private fun executeTextInterface(args: Array<String>) {
    val numberOfAgents = args[1].toInt()
    val demoDuration = args[2].toInt()
    val numberOfMeetings = args[3].toInt()

    val infectionTracerRun = InfectionTracerRun(numberOfAgents, demoDuration, numberOfMeetings)

    val events = mutableListOf<Event>(infectionTracerRun)

    println("Run InfectionTracer demo with ${infectionTracerRun.numberOfAgents} agents and " +
            "duration of ${infectionTracerRun.demoDuration} with ${infectionTracerRun.numberOfMeetings} meetings per cycle.")

    events.addAll(InfectionTracer().demo(infectionTracerRun))

}

class InfectionTracer {

    fun demo(run: InfectionTracerRun) : List<Event> {
        val events = mutableListOf<Event>()

        if (run.numberOfAgents < 2) {
            throw RuntimeException("Should have at least two agents.")
        }
        val encounterService = InMemoryEncounterService()
        val agents = (1..run.numberOfAgents).map { i -> Agent("Agent-$i", encounterService) }.toList()

        for (t in 1..run.demoDuration) {

            println("\n=== cycle $t Encounters registered: ${encounterService.size()} ===")

            println("-- meet")
            val meetings = meet(agents, run.numberOfMeetings)
            meetings.forEach { meeting -> println("${meeting.agent1} meets ${meeting.agent2} at ${meeting.time}") }
            events.addAll(meetings)

            println("-- go see doctor")
            val infections = goSeeTheDoctor(agents)
            infections.forEach { infection -> printInfected(infection) }
            events.addAll(infections)

            println("-- check")
            val checks = checkEncounters(agents)
            checks.forEach { check -> printCheck(check) }
            events.addAll(checks)
        }

        return events
    }

    private fun printInfected(infection: InfectionEvent) {
        when(infection.newState) {
            ContactState.INFECTED -> println("${infection.agent} is confirmed infected!")
            ContactState.POSSIBLY -> println("${infection.agent} is possibly infected!")
            ContactState.SAFE -> { /* do nothing */ }
        }
    }
    private fun printCheck(check: EncounterCheckEvent) {
        when (check.encounterState) {
            ContactState.INFECTED -> println("${check.agent} has one or more confirmed infected contacts!!")
            ContactState.POSSIBLY -> println("${check.agent} has one or more possibly infected contacts!")
            ContactState.SAFE -> println("${check.agent} has no infected contacts.")
        }
    }

    private fun meet(agents: List<Agent>, numberOfMeetings: Int): List<MeetEvent> {
        val meetEvents = mutableListOf<MeetEvent>()
        for (t in 1..numberOfMeetings) {
            val agent1 = randomAgent(agents)
            val agent2: Agent = randomAgentExcept(agents, agent1)

            val hash1 = agent1.currentHash
            val hash2 = agent2.currentHash

            val timestamp = LocalDateTime.now()

            meetEvents.add(MeetEvent(agent1.createAgentInfo(), agent2.createAgentInfo(), timestamp))
            agent1.meetOtherAgent(hash2, timestamp)
            agent2.meetOtherAgent(hash1, timestamp)
        }
        return meetEvents
    }

    private fun randomAgentExcept(agents: List<Agent>, agent1: Agent): Agent {
        var agent2: Agent
        do {
            agent2 = randomAgent(agents)
        } while (agent1 == agent2)
        return agent2
    }

    private fun goSeeTheDoctor(agents: List<Agent>): List<InfectionEvent> {
        val infectionEvents = mutableListOf<InfectionEvent>()
        for (t in 1..1) {
            val agent = randomAgent(agents)
            agent.alertConfirmedInfected()
            infectionEvents.add(InfectionEvent(agent.createAgentInfo(), ContactState.INFECTED))
        }
        for (t in 1..1) {
            val agent = randomAgent(agents)
            agent.alertPossiblyInfected()
            infectionEvents.add(InfectionEvent(agent.createAgentInfo(), ContactState.POSSIBLY))
        }
        return infectionEvents
    }

    private fun randomAgent(agents: List<Agent>) =
            agents[Random.nextInt(0, agents.size)]

    private fun checkEncounters(agents: List<Agent>): List<EncounterCheckEvent> {
        return agents.map { agent -> agent.checkEncounters() }.toCollection(mutableListOf())
    }
}



