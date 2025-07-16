package refactor;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.ltk.core.refactoring.Change;
import model.change.ExtractionPlan;
import model.method.MethodMetrics;
import model.method.MethodMetrics.MethodMetricsBuilder;
import model.common.LocStats;
import model.common.ComplexityStats;
import neo.algorithms.Solution;
import neo.algorithms.Sequence;
import neo.algorithms.exhaustivesearch.EnumerativeSearch;
import neo.refactoringcache.RefactoringCache;
import neo.refactoringcache.ConsecutiveSequenceIterator.APPROACH;

public final class CodeExtractionEngine {

    private final EnumerativeSearch search = new EnumerativeSearch();
    private final RefactoringCache cache  = new RefactoringCache(); // 	quitar final???

    public MethodMetrics analyseAndPlan(ICompilationUnit icu,
                                        CompilationUnit cu,
                                        MethodDeclaration method,
                                        int currentCc,
                                        int currentLoc)
            throws CoreException {

        /* 1. Encontrar la mejor solución */
        Solution solution = search.run(
                APPROACH.PAIRS,
                /*writer*/ null,
                icu.getElementName(),
                cu,
                cache,
                /*runtimeToFillRefactorCache*/ 0L,
                List.of(),
                method,
                currentCc
        );

        /* 2. Convertir la solución en lista Change (apply / undo) */
        List<Change> applyChanges = new ArrayList<>();
        List<Change> undoChanges  = new ArrayList<>();

        if (solution != null) {
            for (Sequence seq : solution.getSequences()) {
                Change c = ChangeBuilder.buildChangeForSequence(seq, icu, cu);
                applyChanges.add(c);
                undoChanges.add(c.getUndoChange(null));
            }
        }

        /* 3. Métricas tras la extracción */
        int refactoredCc  = solution == null ? currentCc  : solution.getComplexityAfter();
        int refactoredLoc = solution == null ? currentLoc : solution.getLocAfter();
        int extractedMethods = solution == null ? 0       : solution.getSequences().size();

        ExtractionPlan plan     = new ExtractionPlan(applyChanges);
        ExtractionPlan undoPlan = new ExtractionPlan(undoChanges);

        return MethodMetrics.builder()
                .name(method.getName().getIdentifier())
                .currentCc(currentCc)
                .refactoredCc(refactoredCc)
                .currentLoc(currentLoc)
                .refactoredLoc(refactoredLoc)
                .extractedMethodCount(extractedMethods)
                .applyPlan(plan)
                .undoPlan(undoPlan)
                .build();
    }
}
