package test.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import main.session.ActionType;

class ActionTypeTest {

    @Test
    void given_enum_when_values_should_returnThreeConstants() {
        final ActionType[] values = ActionType.values();
        assertEquals(3, values.length);
        assertSame(ActionType.CLASS, values[0]);
        assertSame(ActionType.PROJECT, values[1]);
        assertSame(ActionType.WORKSPACE, values[2]);
    }

    @Test
    void given_validName_when_valueOf_should_returnEnumConstant() {
        assertSame(ActionType.PROJECT, ActionType.valueOf("PROJECT"));
    }

    @Test
    void given_unknownName_when_valueOf_throws_illegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ActionType.valueOf("UNKNOWN"));
    }
}
