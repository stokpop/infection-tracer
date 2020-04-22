package nl.stokpop.infection

import java.lang.RuntimeException
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 3) {
        println("Provide number of agents and demo duration and number of meetings" +
                " per cycle as parameters.")
        exitProcess(1)
    }

    val numberOfAgents = args[0].toInt()
    val demoDuration = args[1].toInt()
    val numberOfMeetings = args[2].toInt()

    println("Run InfectionTracer demo with $numberOfAgents agents and " +
            "duration of $demoDuration with $numberOfMeetings meetings per cycle.")
    InfectionTracer().demo(numberOfAgents, demoDuration, numberOfMeetings)
}

class InfectionTracer {

    fun demo(numberOfAgents: Int, demoDuration: Int, numberOfMeetings: Int) {
        if (numberOfAgents < 2) {
            throw RuntimeException("Should have at least two agents.")
        }
        val encounterService = InMemoryEncounterService()
        val agents = (1..numberOfAgents).map { i -> Agent("Agent-$i", encounterService) }.toList()

        for (t in 1..demoDuration) {
            println("=== cycle $t Encounters registered: ${encounterService.size()} ===")
            println("-- meet")
            meet(agents, numberOfMeetings)
            println("-- go see doctor")
            goSeeTheDocter(agents)
            println("-- check")
            checkEncounters(agents)
        }
    }

    private fun meet(agents: List<Agent>, numberOfMeetings: Int) {
        for (t in 1..numberOfMeetings) {

            val nextInt1 = Random.nextInt(0, agents.size)

            // should not be same agent
            var nextInt2: Int
            do {
                nextInt2 = Random.nextInt(0, agents.size)
            } while (nextInt1 == nextInt2)

            val agent1 = agents[nextInt1]
            val agent2 = agents[nextInt2]
            val hash1 = agent1.currentHash
            val hash2 = agent2.currentHash

            val timestamp = LocalDateTime.now()
            println("$agent1 meets $agent2 at $timestamp")
            agent1.meetOtherAgent(hash2, timestamp)
            agent2.meetOtherAgent(hash1, timestamp)
        }
    }

    private fun goSeeTheDocter(agents: List<Agent>) {
        for (t in 1..1) {
            val agent = agents[Random.nextInt(0, agents.size)]
            agent.alertConfirmedInfected()
            println("$agent is confirmed infected!")
        }
        for (t in 1..1) {
            val agent = agents[Random.nextInt(0, agents.size)]
            agent.alertPossiblyInfected()
            println("$agent is possibly infected!")
        }
    }

    private fun checkEncounters(agents: List<Agent>) {
        agents.forEach { agent -> agent.checkEncounters() }
    }
}

data class Encounter(val timestamp: LocalDateTime, val one: ContactHash, val two: ContactHash)

class ContactHash {

    private val STRING_LENGTH: Int = 28
    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    val hash: String = generateContactHash()

    private fun generateContactHash(): String {
        return (1..STRING_LENGTH)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
    }

    override fun toString(): String {
        return "ContactHash(hash='$hash')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContactHash

        if (hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        return hash.hashCode()
    }
}

data class ContactInfo(val encounter: Encounter, var state: ContactState)

enum class ContactState {
    INFECTED, POSSIBLY, SAFE
}

interface EncounterService {
    fun addEncounter(encounter: Encounter)
    fun checkEncounters(encounters: List<Encounter>): ContactState
    fun registerConfirmedInfected(encounters: java.util.ArrayList<Encounter>)
    fun registerPossiblyInfected(encounters: java.util.ArrayList<Encounter>)
    fun size(): Int
}

class InMemoryEncounterService : EncounterService {

    private val contactInfos: ArrayList<ContactInfo> = ArrayList()

    override fun addEncounter(encounter: Encounter) {
        // check if there meeting is not registered yet...
        val exists = contactInfos.filter { contactInfo -> contactInfo.encounter == encounter }.any()
        if (!exists) { contactInfos.add(ContactInfo(encounter, ContactState.SAFE)) }
    }

    override fun checkEncounters(encounters: List<Encounter>): ContactState {

        val contactsMoments = contactInfos.filter { contactInfo -> encounters.contains(contactInfo.encounter) }

        val infected = contactsMoments.filter { contactInfo -> contactInfo.state == ContactState.INFECTED }.any()
        if (infected) {
            return ContactState.INFECTED
        }

        val possibly = contactsMoments.filter { contactInfo -> contactInfo.state == ContactState.POSSIBLY }.any()
        if (possibly) {
            return ContactState.POSSIBLY
        }

        return ContactState.SAFE
    }

    override fun registerConfirmedInfected(encounters: java.util.ArrayList<Encounter>) {
        contactInfos
                .asSequence()
                .filter { c -> encounters.contains(c.encounter) }
                .forEach { c -> c.state = ContactState.INFECTED }
    }

    override fun registerPossiblyInfected(encounters: java.util.ArrayList<Encounter>) {
        contactInfos
                .asSequence()
                .filter { c -> encounters.contains(c.encounter) }
                .filter { c -> c.state == ContactState.INFECTED }
                .forEach { c -> c.state = ContactState.POSSIBLY }
    }

    override fun size(): Int {
        return contactInfos.size
    }
}

class Agent(val name: String, private var encounterService: EncounterService) {

    var currentHash: ContactHash = ContactHash()
    var state: ContactState = ContactState.SAFE

    private val encounters: ArrayList<Encounter> = ArrayList()

    fun alertConfirmedInfected() {
        state = ContactState.INFECTED
        encounterService.registerConfirmedInfected(encounters)
    }

    fun alertPossiblyInfected() {
        if (state != ContactState.INFECTED) {
            state = ContactState.POSSIBLY
        }
        encounterService.registerPossiblyInfected(encounters)
    }

    /**
     * Timestamp need to be the same for both encounters, or they will not match...
     */
    fun meetOtherAgent(otherHash: ContactHash, timestamp: LocalDateTime) {
        val encounter = createEncounter(timestamp, currentHash, otherHash)
        encounters.add(encounter)
        encounterService.addEncounter(encounter)
        // can do this every meeting or every X time
        updateHash()
    }

    private fun createEncounter(timestamp: LocalDateTime, hash1: ContactHash, hash2: ContactHash): Encounter {
        // can this be put in constructor? always same order of hashes,
        // so encounter of two is always the same
        return if (hash1.hash < hash2.hash) {
            Encounter(timestamp, hash1, hash2)
        }
        else {
            Encounter(timestamp, hash2, hash1)
        }
    }

    private fun updateHash() {
        currentHash = ContactHash()
    }

    fun checkEncounters() {
        when (encounterService.checkEncounters(encounters)) {
            ContactState.INFECTED -> println("$this has one or more confirmed infected contacts!!")
            ContactState.POSSIBLY -> println("$this has one or more possibly infected contacts!")
            ContactState.SAFE -> println("$this has no infected contacts.")
        }
    }

    override fun toString(): String {
        return "Agent(name='$name', state=$state)"
    }
}