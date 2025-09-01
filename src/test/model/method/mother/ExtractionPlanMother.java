package test.model.method.mother;

import java.util.List;
import main.model.change.ExtractionPlan;
import org.eclipse.ltk.core.refactoring.Change;

public final class ExtractionPlanMother {
    private ExtractionPlanMother() {}

    public static ExtractionPlan empty() {
        return new ExtractionPlan(List.of());
    }

    public static ExtractionPlan withChanges(final List<Change> changes) {
        return new ExtractionPlan(changes);
    }
}
