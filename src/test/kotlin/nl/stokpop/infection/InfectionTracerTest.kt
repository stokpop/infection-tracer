/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package nl.stokpop.infection

import kotlin.test.Test
import kotlin.test.assertNotNull

class InfectionTracerTest {
    @Test fun testDemoRuns() {
        val classUnderTest = InfectionTracer()
        assertNotNull(classUnderTest.demo(2,2,2), "app should run")
    }
}
