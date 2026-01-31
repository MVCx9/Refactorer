package test.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.preferences.PluginPreferences;

class PluginPreferencesTest {

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@BeforeEach
	void setUp() {
		PluginPreferences.resetCplexState();
	}

	@AfterEach
	void tearDown() {
		PluginPreferences.resetCplexState();
	}

	@Test
	@DisplayName("given_initialState_when_isCplexLoaded_should_returnFalse")
	void given_initialState_when_isCplexLoaded_should_returnFalse() {
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_resetCplexState_when_called_should_setCplexLoadedToFalse")
	void given_resetCplexState_when_called_should_setCplexLoadedToFalse() {
		PluginPreferences.resetCplexState();
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_emptyPath_when_loadCplexFromPath_should_returnFalse")
	void given_emptyPath_when_loadCplexFromPath_should_returnFalse() {
		final boolean result = PluginPreferences.loadCplexFromPath("");
		assertFalse(result);
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_nullPath_when_loadCplexFromPath_should_returnFalse")
	void given_nullPath_when_loadCplexFromPath_should_returnFalse() {
		final boolean result = PluginPreferences.loadCplexFromPath(null);
		assertFalse(result);
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_whitespaceOnlyPath_when_loadCplexFromPath_should_returnFalse")
	void given_whitespaceOnlyPath_when_loadCplexFromPath_should_returnFalse() {
		final boolean result = PluginPreferences.loadCplexFromPath("   ");
		assertFalse(result);
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_tabOnlyPath_when_loadCplexFromPath_should_returnFalse")
	void given_tabOnlyPath_when_loadCplexFromPath_should_returnFalse() {
		final boolean result = PluginPreferences.loadCplexFromPath("\t\t");
		assertFalse(result);
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_invalidPath_when_loadCplexFromPath_should_returnFalseAndNotLoadCplex")
	void given_invalidPath_when_loadCplexFromPath_should_returnFalseAndNotLoadCplex() {
		final boolean result = PluginPreferences.loadCplexFromPath("/nonexistent/path/to/cplex");
		assertFalse(result);
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_pathWithOnlySpaces_when_loadCplexFromPath_should_returnFalse")
	void given_pathWithOnlySpaces_when_loadCplexFromPath_should_returnFalse() {
		final boolean result = PluginPreferences.loadCplexFromPath("     ");
		assertFalse(result);
	}

	@Test
	@DisplayName("given_pathWithNewlines_when_loadCplexFromPath_should_returnFalse")
	void given_pathWithNewlines_when_loadCplexFromPath_should_returnFalse() {
		final boolean result = PluginPreferences.loadCplexFromPath("\n\n");
		assertFalse(result);
	}

	@Test
	@DisplayName("given_cplexNotLoaded_when_isCplexLoaded_should_returnFalse")
	void given_cplexNotLoaded_when_isCplexLoaded_should_returnFalse() {
		PluginPreferences.resetCplexState();
		final boolean loaded = PluginPreferences.isCplexLoaded();
		assertFalse(loaded);
	}

	@Test
	@DisplayName("given_resetCplexState_when_calledMultipleTimes_should_remainFalse")
	void given_resetCplexState_when_calledMultipleTimes_should_remainFalse() {
		PluginPreferences.resetCplexState();
		PluginPreferences.resetCplexState();
		PluginPreferences.resetCplexState();
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_invalidPathAfterReset_when_loadCplexFromPath_should_returnFalse")
	void given_invalidPathAfterReset_when_loadCplexFromPath_should_returnFalse() {
		PluginPreferences.resetCplexState();
		final boolean result = PluginPreferences.loadCplexFromPath("/invalid/path");
		assertFalse(result);
	}

	@Test
	@DisplayName("given_emptyStringPath_when_loadCplexFromPath_should_notChangeCplexLoadedState")
	void given_emptyStringPath_when_loadCplexFromPath_should_notChangeCplexLoadedState() {
		PluginPreferences.resetCplexState();
		final boolean beforeLoad = PluginPreferences.isCplexLoaded();
		PluginPreferences.loadCplexFromPath("");
		final boolean afterLoad = PluginPreferences.isCplexLoaded();
		toStrictEqual(beforeLoad, afterLoad);
	}

	@Test
	@DisplayName("given_nullPath_when_loadCplexFromPath_should_notThrowException")
	void given_nullPath_when_loadCplexFromPath_should_notThrowException() {
		boolean exceptionThrown = false;
		try {
			PluginPreferences.loadCplexFromPath(null);
		} catch (final Exception e) {
			exceptionThrown = true;
		}
		assertFalse(exceptionThrown);
	}

	@Test
	@DisplayName("given_pathToNonExistentDirectory_when_loadCplexFromPath_should_returnFalse")
	void given_pathToNonExistentDirectory_when_loadCplexFromPath_should_returnFalse() {
		final boolean result = PluginPreferences.loadCplexFromPath("/this/directory/does/not/exist");
		assertFalse(result);
	}

	@Test
	@DisplayName("given_pathWithSpecialCharacters_when_loadCplexFromPath_should_returnFalse")
	void given_pathWithSpecialCharacters_when_loadCplexFromPath_should_returnFalse() {
		final boolean result = PluginPreferences.loadCplexFromPath("/path/with/special/chars/!@#$%");
		assertFalse(result);
	}

	@Test
	@DisplayName("given_validLookingPath_when_loadCplexFromPath_should_returnFalseIfLibNotFound")
	void given_validLookingPath_when_loadCplexFromPath_should_returnFalseIfLibNotFound() {
		final boolean result = PluginPreferences.loadCplexFromPath("/usr/local/lib");
		assertFalse(result);
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_homeDirectoryPath_when_loadCplexFromPath_should_returnFalseIfLibNotFound")
	void given_homeDirectoryPath_when_loadCplexFromPath_should_returnFalseIfLibNotFound() {
		final String homePath = System.getProperty("user.home");
		final boolean result = PluginPreferences.loadCplexFromPath(homePath);
		assertFalse(result);
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_pathWithTrailingSpaces_when_loadCplexFromPath_should_trimAndProcess")
	void given_pathWithTrailingSpaces_when_loadCplexFromPath_should_trimAndProcess() {
		final boolean result = PluginPreferences.loadCplexFromPath("  /invalid/path  ");
		assertFalse(result);
	}

	@Test
	@DisplayName("given_isCplexLoadedFalse_when_resetCplexState_should_remainFalse")
	void given_isCplexLoadedFalse_when_resetCplexState_should_remainFalse() {
		assertFalse(PluginPreferences.isCplexLoaded());
		PluginPreferences.resetCplexState();
		assertFalse(PluginPreferences.isCplexLoaded());
	}

	@Test
	@DisplayName("given_multipleCalls_when_loadCplexFromPathWithNull_should_alwaysReturnFalse")
	void given_multipleCalls_when_loadCplexFromPathWithNull_should_alwaysReturnFalse() {
		assertFalse(PluginPreferences.loadCplexFromPath(null));
		assertFalse(PluginPreferences.loadCplexFromPath(null));
		assertFalse(PluginPreferences.loadCplexFromPath(null));
	}

	@Test
	@DisplayName("given_multipleCalls_when_loadCplexFromPathWithEmpty_should_alwaysReturnFalse")
	void given_multipleCalls_when_loadCplexFromPathWithEmpty_should_alwaysReturnFalse() {
		assertFalse(PluginPreferences.loadCplexFromPath(""));
		assertFalse(PluginPreferences.loadCplexFromPath(""));
		assertFalse(PluginPreferences.loadCplexFromPath(""));
	}
}
