package test.preferences;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.preferences.PluginPreferences;

class PluginPreferencesTest {

    @BeforeEach
    void setUp() {
        PluginPreferences.resetCplexState();
    }

    @Test
    void given_emptyPath_when_loadCplexFromPath_should_returnFalse() {
        assertFalse(PluginPreferences.loadCplexFromPath(""));
        assertFalse(PluginPreferences.isCplexLoaded());
    }

    @Test
    void given_nullPath_when_loadCplexFromPath_should_returnFalse() {
        assertFalse(PluginPreferences.loadCplexFromPath(null));
        assertFalse(PluginPreferences.isCplexLoaded());
    }

    @Test
    void given_blankPath_when_loadCplexFromPath_should_returnFalse() {
        assertFalse(PluginPreferences.loadCplexFromPath("   "));
    }

    @Test
    void given_nonexistentPath_when_loadCplexFromPath_should_returnFalse() {
        assertFalse(PluginPreferences.loadCplexFromPath("/non/existent/path/that/cannot/exist"));
        assertFalse(PluginPreferences.isCplexLoaded());
    }

    @Test
    void given_loadedState_when_resetCplexState_should_clearFlag() {
        PluginPreferences.resetCplexState();
        assertFalse(PluginPreferences.isCplexLoaded());
    }

}
