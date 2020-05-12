package nl.stokpop.infection.event

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InfectionTracerRun(
        val numberOfAgents: Int = 4,
        val demoDuration: Int = 4,
        val numberOfMeetings: Int = 4
) : Event
