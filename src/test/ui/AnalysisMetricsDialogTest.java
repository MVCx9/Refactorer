package test.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import main.session.ActionType;
import main.ui.AnalysisMetricsDialog;
import test.objectmothers.ClassMetricsMother;
import test.objectmothers.ProjectMetricsMother;
import test.objectmothers.WorkspaceMetricsMother;

class AnalysisMetricsDialogTest {

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
    void given_classMetrics_when_construct_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        final AnalysisMetricsDialog d = new AnalysisMetricsDialog(shell, ActionType.CLASS,
                ClassMetricsMother.withRefactors("A.java"), "before", "after");
        assertNotNull(d);
    }

    @Test
    void given_projectMetrics_when_constructWithoutSources_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        final AnalysisMetricsDialog d = new AnalysisMetricsDialog(shell, ActionType.PROJECT,
                ProjectMetricsMother.withRefactors("P"));
        assertNotNull(d);
    }

    @Test
    void given_workspaceMetrics_when_constructWithLegacyConstructor_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        final AnalysisMetricsDialog d = new AnalysisMetricsDialog(shell, ActionType.WORKSPACE,
                WorkspaceMetricsMother.withRefactors(), null, null, null);
        assertNotNull(d);
    }

    @Test
    void given_dialog_when_setReadOnly_should_returnSameInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        final AnalysisMetricsDialog d = new AnalysisMetricsDialog(shell, ActionType.CLASS,
                ClassMetricsMother.simple("A.java"));
        final AnalysisMetricsDialog returned = d.setReadOnly(true);
        assertNotNull(returned);
    }
}
