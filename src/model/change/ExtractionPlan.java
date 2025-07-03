package model.change;

import java.util.List;
import org.eclipse.ltk.core.refactoring.Change;

public record ExtractionPlan(List<Change> changes) {}