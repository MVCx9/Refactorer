package test.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;

class CommonInterfacesTest {

    @Test
    void given_identifiableWithName_when_getNameUpper_should_returnUpperCase() {
        final Identifiable id = () -> "foo";
        assertEquals("FOO", id.getNameUpper());
    }

    @Test
    void given_identifiableWithNullName_when_getNameUpper_should_returnEmpty() {
        final Identifiable id = () -> null;
        assertEquals("", id.getNameUpper());
    }

    @Test
    void given_complexityStats_when_default_should_computeImprovement() {
        final ComplexityStats stats = stub(10, 4);
        assertEquals(6, stats.getImprovementCc());
        assertEquals(60.0, stats.getImprovementPercentCc(), 0.0001);
        assertTrue(stats.isImprovedCc());
    }

    @Test
    void given_complexityStatsWithZeroCurrent_when_improvement_should_returnZeroPercent() {
        final ComplexityStats stats = stub(0, 0);
        assertEquals(0.0, stats.getImprovementPercentCc(), 0.0001);
        assertFalse(stats.isImprovedCc());
    }

    @Test
    void given_locStats_when_default_should_computeImprovement() {
        final LocStats stats = locStub(20, 8);
        assertEquals(12, stats.getImprovementLoc());
        assertEquals(60.0, stats.getImprovementPercentLoc(), 0.0001);
        assertTrue(stats.isImprovedLoc());
    }

    @Test
    void given_locStatsWithZeroCurrent_when_improvement_should_returnZeroPercent() {
        final LocStats stats = locStub(0, 0);
        assertEquals(0.0, stats.getImprovementPercentLoc(), 0.0001);
        assertFalse(stats.isImprovedLoc());
    }

    private ComplexityStats stub(final int current, final int refactored) {
        return new ComplexityStats() {
            @Override public int getCurrentCc() { return current; }
            @Override public int getRefactoredCc() { return refactored; }
        };
    }

    private LocStats locStub(final int current, final int refactored) {
        return new LocStats() {
            @Override public int getCurrentLoc() { return current; }
            @Override public int getRefactoredLoc() { return refactored; }
        };
    }
}
