package nl.stokpop.infection.event

import com.squareup.moshi.JsonClass
import nl.stokpop.infection.Agent
import nl.stokpop.infection.ContactState
import nl.stokpop.infection.Encounter

@JsonClass(generateAdapter = true)
data class InfectionEvent(
        val agent: AgentInfo,
        val newState: ContactState
) : Event
