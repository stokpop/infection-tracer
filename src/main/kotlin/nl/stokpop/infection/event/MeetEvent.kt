package nl.stokpop.infection.event

import com.squareup.moshi.JsonClass
import nl.stokpop.infection.Agent
import nl.stokpop.infection.ContactState
import nl.stokpop.infection.Encounter
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class MeetEvent(
        val agent1: AgentInfo,
        val agent2: AgentInfo,
        val time: LocalDateTime
) : Event
