package nl.stokpop.infection

import com.squareup.moshi.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : JsonAdapter<LocalDateTime>() {

    companion object {
        private val FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    override fun toJson(writer: JsonWriter, value: LocalDateTime?) {
        val localDateTime = FORMATTER.format(value)
        writer.value(localDateTime)
    }

    override fun fromJson(reader: JsonReader): LocalDateTime? {
        val value = reader.nextString()
        return LocalDateTime.parse(value, FORMATTER)
    }
}