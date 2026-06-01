package test.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import main.session.ActionType;
import main.session.SessionAnalysisStore;
import main.ui.AnalysisHistoryDialog;
import main.ui.AnalysisNoRefactorDialog;
import main.ui.ConfigurationDialog;
import main.ui.ErrorDetailsDialog;
import main.ui.IndividualReviewDialog;
import main.ui.RefactorConfirmationDialog.SelectedClassInfo;
import test.objectmothers.ClassMetricsMother;
import test.objectmothers.ProjectMetricsMother;
import test.objectmothers.WorkspaceMetricsMother;

class OtherDialogsTest {

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
    void given_historyEntries_when_constructHistoryDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        SessionAnalysisStore.getInstance().clear();
        SessionAnalysisStore.getInstance().register(ActionType.CLASS, ClassMetricsMother.simple("A.java"));
        assertNotNull(new AnalysisHistoryDialog(shell, SessionAnalysisStore.getInstance().getHistory()));
    }

    @Test
    void given_nullHistory_when_constructHistoryDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        assertNotNull(new AnalysisHistoryDialog(shell, null));
    }

    @Test
    void given_classMetrics_when_constructNoRefactorDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        assertNotNull(new AnalysisNoRefactorDialog(shell, ActionType.CLASS,
                ClassMetricsMother.simple("A.java"), null));
    }

    @Test
    void given_projectMetrics_when_constructNoRefactorDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        assertNotNull(new AnalysisNoRefactorDialog(shell, ActionType.PROJECT,
                ProjectMetricsMother.simple("P"), null));
    }

    @Test
    void given_workspaceMetrics_when_constructNoRefactorDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        assertNotNull(new AnalysisNoRefactorDialog(shell, ActionType.WORKSPACE,
                WorkspaceMetricsMother.simple(), null));
    }

    @Test
    void given_nullMetrics_when_constructNoRefactorDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        assertNotNull(new AnalysisNoRefactorDialog(shell, ActionType.CLASS, null, null));
    }

    @Test
    void given_validShell_when_constructConfigurationDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        assertNotNull(new ConfigurationDialog(shell));
    }

    @Test
    void given_throwable_when_constructErrorDetailsDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        assertNotNull(new ErrorDetailsDialog(shell, "title", new RuntimeException("boom")));
    }

    @Test
    void given_nullTitleAndError_when_constructErrorDetailsDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        assertNotNull(new ErrorDetailsDialog(shell, null, null));
    }

    @Test
    void given_selectedClasses_when_constructIndividualReviewDialog_should_returnInstance() {
        Assumptions.assumeTrue(shell != null, "SWT Display not available");
        final SelectedClassInfo info = new SelectedClassInfo("P", ClassMetricsMother.withRefactors("A.java"));
        assertNotNull(new IndividualReviewDialog(shell, List.of(info), true));
    }
}
