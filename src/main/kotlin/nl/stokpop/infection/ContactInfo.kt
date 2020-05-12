package nl.stokpop.infection

data class ContactInfo(val encounter: Encounter, var state: ContactState)

enum class ContactState {
    INFECTED, POSSIBLY, SAFE
}
