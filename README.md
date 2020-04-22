# infection-tracer

Demo of a contacts or encounters tracing code concept.

This is to investigate some principles of anonymous contact or encounters tracing.

### Principles

   * encounters not traceable to actual users
   * encounter data from user stays on device
   * it is not known to users what are the infected encounter(s)
   * two infections states are now in place: possibly infected and confirmed infected (by official test)
     * confirmed infected should be done by authorised party (e.g. doctor)
     * possibly infected could be done by user itself

### Other points

   * Does this scale? how large do the data sets get? per agent and in encounter service
   * No user data is stored on encounter service, only the encounter data (see below)
   * Agents should periodically call to check their encounters (pull not push)
   * How to technically prevent a particular set of encounters to be traced back to a known user
     * When agent does checkup with encounter service the agent now provides its list of encounters
     * If this call can be traced back to user (e.g. by logging the IP or other "fingerprint" data with the request) the anonymity goes away.
    
### Data model

The Encounter is a data tuple:

    <timestamp, hash1, hash2>
    
Agents generate a new hash for each encounter. Only the agent knows that this
encounter belongs to itself.

In the EncounterService this Encounter can be marked as SAFE, POSSIBLY or INFECTED.

### General setup

Agents meet. Both agents send an encounter to the encounter service.

Agents do checkups at doctor. When infection is confirmed, the agent sends its
list of known encounters to the encounter service to be marked as infected.

Agents periodically send their list of encounters to the encounter service to
be checked for possible or confirmed encounters with infections.

Agents run on personal devices (e.g. app on phone).

EncounterService runs centrally (e.g. in the cloud).

### Points of attention

   * When an agents registers as infected, in subsequent check it will see "you have infected contacts", but that includes is its own encounter marked as INFECTED.

### Run demo

To run give three parameters, number of agents, number of cycles and number of encounters per cycle.
 
    ./gradlew run --args="3 2 2"
    
Play around with different settings.

### Example output

    Run InfectionTracer demo with 3 agents and duration of 2 with 1 meetings per cycle.
    === cycle 1 Encounters registered: 0 ===
    -- meet
    Agent(name='Agent-1', state=SAFE) meets Agent(name='Agent-2', state=SAFE) at 2020-04-22T12:40:11.002
    -- go see doctor
    Agent(name='Agent-2', state=INFECTED) is confirmed infected!
    Agent(name='Agent-3', state=POSSIBLY) is possibly infected!
    -- check
    Agent(name='Agent-1', state=SAFE) has one or more confirmed infected contacts!!
    Agent(name='Agent-2', state=INFECTED) has one or more confirmed infected contacts!!
    Agent(name='Agent-3', state=POSSIBLY) has no infected contacts.
    === cycle 2 Encounters registered: 1 ===
    -- meet
    Agent(name='Agent-3', state=POSSIBLY) meets Agent(name='Agent-1', state=SAFE) at 2020-04-22T12:40:11.018
    -- go see doctor
    Agent(name='Agent-3', state=INFECTED) is confirmed infected!
    Agent(name='Agent-3', state=INFECTED) is possibly infected!
    -- check
    Agent(name='Agent-1', state=SAFE) has one or more confirmed infected contacts!!
    Agent(name='Agent-2', state=INFECTED) has one or more confirmed infected contacts!!
    Agent(name='Agent-3', state=INFECTED) has one or more possibly infected contacts!