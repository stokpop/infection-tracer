package nl.stokpop.infection.event

import com.squareup.moshi.JsonClass
import nl.stokpop.infection.Agent
import nl.stokpop.infection.ContactState
import nl.stokpop.infection.Encounter

@JsonClass(generateAdapter = true)
data class EncounterCheckEvent(
        val agent: AgentInfo,
        val encounterState: ContactState
) : Event
