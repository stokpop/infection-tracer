package nl.stokpop.infection

import nl.stokpop.infection.event.AgentInfo
import nl.stokpop.infection.event.EncounterCheckEvent
import java.time.LocalDateTime

class Agent(val name: String, private var encounterService: EncounterService) {

    var currentHash: ContactHash = ContactHash()
    var state: ContactState = ContactState.SAFE

    private val encounters = mutableListOf<Encounter>()

    fun createAgentInfo() : AgentInfo = AgentInfo(name, state)

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

    fun checkEncounters() : EncounterCheckEvent {
        val agentInfo = createAgentInfo()
        return when (encounterService.checkEncounters(encounters)) {
            ContactState.INFECTED -> EncounterCheckEvent(agentInfo, ContactState.INFECTED)
            ContactState.POSSIBLY -> EncounterCheckEvent(agentInfo, ContactState.POSSIBLY)
            ContactState.SAFE -> EncounterCheckEvent(agentInfo, ContactState.SAFE)
        }
    }

    override fun toString(): String {
        return "Agent(name='$name', state=$state)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Agent

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }


}