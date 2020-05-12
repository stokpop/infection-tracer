package nl.stokpop.infection

import nl.stokpop.infection.event.InfectionTracerRun
import kotlin.test.Test
import kotlin.test.assertNotNull

class InfectionTracerTest {
    @Test fun testDemoRuns() {
        val classUnderTest = InfectionTracer()
        assertNotNull(classUnderTest.demo(InfectionTracerRun(2,2,2)), "app should run")
    }
}
