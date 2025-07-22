package refactor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.ltk.core.refactoring.Change;

import model.change.ExtractionPlan;
import model.method.MethodMetrics;
import neo.algorithms.Sequence;
import neo.algorithms.Solution;
import neo.algorithms.exhaustivesearch.EnumerativeSearch;
import neo.refactoringcache.ConsecutiveSequenceIterator.APPROACH;
import neo.refactoringcache.RefactoringCache;

public final class CodeExtractionEngine {

    private final EnumerativeSearch search = new EnumerativeSearch();
    private RefactoringCache cache;

    public MethodMetrics analyseAndPlan(ICompilationUnit icu, CompilationUnit cu, MethodDeclaration method, int currentCc, int currentLoc) throws CoreException, IOException {

    	cache = new RefactoringCache(cu);
    	
        /* 1. Encontrar la mejor solución */
        Solution solution = search.run(
                APPROACH.LONG_SEQUENCE_FIRST,
                null,
                icu.getElementName(),
                cu,
                cache,
                0L,
                List.of(),
                method,
                currentCc
        );

        /* 2. Convertir la solución en lista Change (apply / undo) */
        List<Change> applyChanges = new ArrayList<>();
        List<Change> undoChanges  = new ArrayList<>();

        if (solution != null) {
            for (Sequence seq : solution.getSequenceList()) {
                Change doChange = ChangeBuilder.buildChangeForSequence(seq, icu, cu);
                applyChanges.add(doChange);
            }
        }

        /* 3. Métricas tras la extracción */
        
        // complejidad antes de los cambios
        int refactoredCc  = solution == null ? currentCc  : solution.getInitialComplexity();
        // complejidad cognitiva despues de los cambios
        int refactoredLoc = solution == null ? currentLoc : solution.getReducedComplexity();
        // cuantas extracinoes de código
        int extractedMethods = solution == null ? 0       : solution.getSequenceList().size();

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
