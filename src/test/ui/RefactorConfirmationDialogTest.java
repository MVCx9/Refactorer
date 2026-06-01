package test.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import main.model.clazz.ClassMetrics;
import main.ui.RefactorConfirmationDialog;
import main.ui.RefactorConfirmationDialog.SelectedClassInfo;
import test.objectmothers.ClassMetricsMother;

class RefactorConfirmationDialogTest {

    private static Display display;
    private static Shell shell;

    @BeforeAll
    static void setUpClass() {
        display = TestDisplaySupport.getOrCreateDisplay();
        if (display != null) {
            shell = TestDisplaySupport.createHiddenShell(display);
        }
    }

    @AfterAll
    static void tearDownClass() {
        if (shell != null && !shell.isDisposed()) {
            shell.dispose();
        }
    }

    @Test
    void given_projectAndClass_when_constructSelectedClassInfo_should_storeFields() {
        final ClassMetrics cm = ClassMetricsMother.simple("A.java");
        final SelectedClassInfo info = new SelectedClassInfo("ProjectX", cm);
        assertEquals("ProjectX", info.projectName);
        assertSame(cm, info.classMetrics);
    }

    @Test
    void given_validShell_when_constructDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        final RefactorConfirmationDialog dialog = new RefactorConfirmationDialog(shell, List.of(), true);
        assertNotNull(dialog);
    }

    @Test
    void given_constants_when_read_should_haveExpectedValues() {
        assertEquals(1001, RefactorConfirmationDialog.APPLY_ALL);
        assertEquals(1002, RefactorConfirmationDialog.SELECT_INDIVIDUALLY);
    }
}
