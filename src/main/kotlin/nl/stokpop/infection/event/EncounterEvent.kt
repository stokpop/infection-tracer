package nl.stokpop.infection.event

import com.squareup.moshi.JsonClass
import nl.stokpop.infection.Agent
import nl.stokpop.infection.Encounter

@JsonClass(generateAdapter = true)
data class EncounterEvent(
        val agent1: AgentInfo,
        val agent2: AgentInfo,
        val encounter: Encounter
) : Event
