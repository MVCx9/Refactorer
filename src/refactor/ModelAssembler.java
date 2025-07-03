package refactor;


import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import analyzer.ComplexityAnalyzer;
import model.clazz.ClassMetrics;
import model.common.LocStats;
import model.method.MethodMetrics;
import model.project.ProjectMetrics;
import model.workspace.WorkspaceMetrics;

public final class ModelAssembler {

    private final CodeExtractionEngine extractor = new CodeExtractionEngine();

    /** Construye WorkspaceMetrics completo en una sola pasada */
    public WorkspaceMetrics buildWorkspace(IWorkspaceRoot root) { … }

    private ProjectMetrics buildProject(IProject project) { … }

    private ClassMetrics buildClass(ICompilationUnit icu) throws CoreException {
        CompilationUnit cu = parseUnit(icu);
        List<MethodMetrics> methods = new ArrayList<>();

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                int currentCc  = ComplexityAnalyzer.compute(node);
                int currentLoc = loc(node);
                try {
                    MethodMetrics mm = extractor.analyseAndPlan(
                            icu, cu, node, currentCc, currentLoc);
                    methods.add(mm);
                } catch (CoreException e) { /* log */ }
                return false;
            }
        });

        // Agregar métricas de clase
        return new ClassMetricsBuilder()
                .name(cu.getTypeRoot().getElementName().replace(".java", ""))
                .currentLoc(sum(methods, LocStats::getCurrentLoc))
                .refactoredLoc(sum(methods, LocStats::getRefactoredLoc))
                .methods(methods)
                .build();
    }

    /* utilidades parseUnit, loc(), sum(), … */
}
