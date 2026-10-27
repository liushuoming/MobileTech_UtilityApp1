package au.edu.jcu.assessment.utilityapp

import au.edu.jcu.assessment.utilityapp.model.AqiCategory
import au.edu.jcu.assessment.utilityapp.model.AqiScale
import org.junit.Assert.assertEquals
import org.junit.Test

/** Checks the AQI band boundaries for both standards. */
class AqiCategoryTest {

    @Test
    fun usScale_boundaries() {
        assertEquals(AqiCategory.GOOD, AqiCategory.from(50, AqiScale.US))
        assertEquals(AqiCategory.MODERATE, AqiCategory.from(51, AqiScale.US))
        assertEquals(AqiCategory.SENSITIVE, AqiCategory.from(150, AqiScale.US))
        assertEquals(AqiCategory.UNHEALTHY, AqiCategory.from(200, AqiScale.US))
        assertEquals(AqiCategory.VERY_UNHEALTHY, AqiCategory.from(300, AqiScale.US))
        assertEquals(AqiCategory.HAZARDOUS, AqiCategory.from(301, AqiScale.US))
    }

    @Test
    fun europeanScale_boundaries() {
        assertEquals(AqiCategory.GOOD, AqiCategory.from(20, AqiScale.EUROPEAN))
        assertEquals(AqiCategory.FAIR, AqiCategory.from(21, AqiScale.EUROPEAN))
        assertEquals(AqiCategory.MODERATE, AqiCategory.from(60, AqiScale.EUROPEAN))
        assertEquals(AqiCategory.HAZARDOUS, AqiCategory.from(101, AqiScale.EUROPEAN))
    }
}
