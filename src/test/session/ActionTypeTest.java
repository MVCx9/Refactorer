package test.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.session.ActionType;

class ActionTypeTest {

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_actionType_when_values_should_returnThreeValues")
	void given_actionType_when_values_should_returnThreeValues() {
		final ActionType[] values = ActionType.values();
		toStrictEqual(3, values.length);
	}

	@Test
	@DisplayName("given_classValue_when_valueOf_should_returnClass")
	void given_classValue_when_valueOf_should_returnClass() {
		final ActionType value = ActionType.valueOf("CLASS");
		toStrictEqual(ActionType.CLASS, value);
	}

	@Test
	@DisplayName("given_projectValue_when_valueOf_should_returnProject")
	void given_projectValue_when_valueOf_should_returnProject() {
		final ActionType value = ActionType.valueOf("PROJECT");
		toStrictEqual(ActionType.PROJECT, value);
	}

	@Test
	@DisplayName("given_workspaceValue_when_valueOf_should_returnWorkspace")
	void given_workspaceValue_when_valueOf_should_returnWorkspace() {
		final ActionType value = ActionType.valueOf("WORKSPACE");
		toStrictEqual(ActionType.WORKSPACE, value);
	}

	@Test
	@DisplayName("given_class_when_name_should_returnCLASS")
	void given_class_when_name_should_returnCLASS() {
		toStrictEqual("CLASS", ActionType.CLASS.name());
	}

	@Test
	@DisplayName("given_project_when_name_should_returnPROJECT")
	void given_project_when_name_should_returnPROJECT() {
		toStrictEqual("PROJECT", ActionType.PROJECT.name());
	}

	@Test
	@DisplayName("given_workspace_when_name_should_returnWORKSPACE")
	void given_workspace_when_name_should_returnWORKSPACE() {
		toStrictEqual("WORKSPACE", ActionType.WORKSPACE.name());
	}

	@Test
	@DisplayName("given_class_when_ordinal_should_returnZero")
	void given_class_when_ordinal_should_returnZero() {
		toStrictEqual(0, ActionType.CLASS.ordinal());
	}

	@Test
	@DisplayName("given_project_when_ordinal_should_returnOne")
	void given_project_when_ordinal_should_returnOne() {
		toStrictEqual(1, ActionType.PROJECT.ordinal());
	}

	@Test
	@DisplayName("given_workspace_when_ordinal_should_returnTwo")
	void given_workspace_when_ordinal_should_returnTwo() {
		toStrictEqual(2, ActionType.WORKSPACE.ordinal());
	}

	@Test
	@DisplayName("given_actionType_when_valuesContainsClass_should_returnTrue")
	void given_actionType_when_valuesContainsClass_should_returnTrue() {
		boolean containsClass = false;
		for (final ActionType type : ActionType.values()) {
			if (type == ActionType.CLASS) {
				containsClass = true;
				break;
			}
		}
		assertTrue(containsClass);
	}

	@Test
	@DisplayName("given_actionType_when_valuesContainsProject_should_returnTrue")
	void given_actionType_when_valuesContainsProject_should_returnTrue() {
		boolean containsProject = false;
		for (final ActionType type : ActionType.values()) {
			if (type == ActionType.PROJECT) {
				containsProject = true;
				break;
			}
		}
		assertTrue(containsProject);
	}

	@Test
	@DisplayName("given_actionType_when_valuesContainsWorkspace_should_returnTrue")
	void given_actionType_when_valuesContainsWorkspace_should_returnTrue() {
		boolean containsWorkspace = false;
		for (final ActionType type : ActionType.values()) {
			if (type == ActionType.WORKSPACE) {
				containsWorkspace = true;
				break;
			}
		}
		assertTrue(containsWorkspace);
	}

	@Test
	@DisplayName("given_actionType_when_toString_should_returnEnumName")
	void given_actionType_when_toString_should_returnEnumName() {
		toStrictEqual("CLASS", ActionType.CLASS.toString());
		toStrictEqual("PROJECT", ActionType.PROJECT.toString());
		toStrictEqual("WORKSPACE", ActionType.WORKSPACE.toString());
	}

	@Test
	@DisplayName("given_invalidValue_when_valueOf_throws_IllegalArgumentException")
	void given_invalidValue_when_valueOf_throws_IllegalArgumentException() {
		try {
			ActionType.valueOf("INVALID");
			assertTrue(false);
		} catch (final IllegalArgumentException e) {
			assertNotNull(e);
		}
	}
}
