package nl.stokpop.infection

import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class Encounter(val timestamp: LocalDateTime, val one: ContactHash, val two: ContactHash)
