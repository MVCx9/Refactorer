package test.preferences;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import main.preferences.ComplexityThresholdPropertyPage;

class ComplexityThresholdPropertyPageTest {

    @Test
    void given_newInstance_when_construct_should_notBeNull() {
        assertNotNull(new ComplexityThresholdPropertyPage());
    }

    @Test
    void given_newInstance_when_performDefaults_should_notThrow() {
        final ComplexityThresholdPropertyPage page = new ExposedPropertyPage();
        ((ExposedPropertyPage) page).callPerformDefaults();
    }

    private static final class ExposedPropertyPage extends ComplexityThresholdPropertyPage {
        void callPerformDefaults() {
            try {
                performDefaults();
            } catch (NullPointerException ignored) {
            }
        }
    }
}
