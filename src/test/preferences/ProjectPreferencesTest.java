package test.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import main.neo.app.Constants;
import main.preferences.ProjectPreferences;

class ProjectPreferencesTest {

    @Test
    void given_nullProject_when_getComplexityThreshold_should_returnDefault() {
        assertEquals(Constants.COGNITIVE_COMPLEXITY_THRESHOLD, ProjectPreferences.getComplexityThreshold(null));
    }

    @Test
    void given_nullProject_when_setComplexityThreshold_should_doNothing() {
        ProjectPreferences.setComplexityThreshold(null, 50);
        assertEquals(Constants.COGNITIVE_COMPLEXITY_THRESHOLD, ProjectPreferences.getComplexityThreshold(null));
    }
}
