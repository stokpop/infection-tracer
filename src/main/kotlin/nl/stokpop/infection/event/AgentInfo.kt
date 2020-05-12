package nl.stokpop.infection.event

import com.squareup.moshi.JsonClass
import nl.stokpop.infection.Agent
import nl.stokpop.infection.ContactState
import nl.stokpop.infection.Encounter

@JsonClass(generateAdapter = true)
data class AgentInfo(
        val name: String,
        val state: ContactState
) : Event
