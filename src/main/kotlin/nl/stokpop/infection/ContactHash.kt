package nl.stokpop.infection

import com.squareup.moshi.JsonClass
import kotlin.random.Random

@JsonClass(generateAdapter = true)
class ContactHash(val hash : String) {

    constructor() : this(generateContactHash())

    companion object {
        private const val STRING_LENGTH = 28
        private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        fun generateContactHash(): String {
            return (1..STRING_LENGTH)
                    .map { Random.nextInt(0, charPool.size) }
                    .map(charPool::get)
                    .joinToString("")
        }
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