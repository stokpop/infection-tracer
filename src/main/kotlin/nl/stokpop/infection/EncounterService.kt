package nl.stokpop.infection

interface EncounterService {
    fun addEncounter(encounter: Encounter)
    fun checkEncounters(encounters: List<Encounter>): ContactState
    fun registerConfirmedInfected(encounters: List<Encounter>)
    fun registerPossiblyInfected(encounters: List<Encounter>)
    fun size(): Int
}

class InMemoryEncounterService : EncounterService {

    private val contactInfos = mutableListOf<ContactInfo>()

    override fun addEncounter(encounter: Encounter) {
        // check if there meeting is not registered yet...
        val exists = contactInfos.filter { contactInfo -> contactInfo.encounter == encounter }.any()
        if (!exists) { contactInfos.add(ContactInfo(encounter, ContactState.SAFE)) }
    }

    override fun checkEncounters(encounters: List<Encounter>): ContactState {

        val contactsMoments = contactInfos.filter { contactInfo -> encounters.contains(contactInfo.encounter) }

        val infected = contactsMoments.filter { contactInfo -> contactInfo.state == ContactState.INFECTED }.any()
        if (infected) {
            return ContactState.INFECTED
        }

        val possibly = contactsMoments.filter { contactInfo -> contactInfo.state == ContactState.POSSIBLY }.any()
        if (possibly) {
            return ContactState.POSSIBLY
        }

        return ContactState.SAFE
    }

    override fun registerConfirmedInfected(encounters: List<Encounter>) {
        contactInfos
                .asSequence()
                .filter { c -> encounters.contains(c.encounter) }
                .forEach { c -> c.state = ContactState.INFECTED }
    }

    override fun registerPossiblyInfected(encounters: List<Encounter>) {
        contactInfos
                .asSequence()
                .filter { c -> encounters.contains(c.encounter) }
                .filter { c -> c.state == ContactState.INFECTED }
                .forEach { c -> c.state = ContactState.POSSIBLY }
    }

    override fun size(): Int {
        return contactInfos.size
    }
}
