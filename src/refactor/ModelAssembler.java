package refactor;


import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import analyzer.ComplexityAnalyzer;
import model.clazz.ClassMetrics;
import model.common.LocStats;
import model.method.MethodMetrics;
import model.project.ProjectMetrics;
import model.workspace.WorkspaceMetrics;

/**
 * Construye la jerarquía Workspace→Project→Class→Method rellenando
 * todas las métricas y planes de extracción.
 */
public final class ModelAssembler {

    private final CodeExtractionEngine extractor = new CodeExtractionEngine();

    /** Crea un WorkspaceMetrics para todos los proyectos Java abiertos. */
    public WorkspaceMetrics buildWorkspace(IWorkspaceRoot root) {
        List<ProjectMetrics> projects = new ArrayList<>();

        for (IProject project : root.getProjects()) {
            try {
                if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
                    projects.add(buildProject(project));
                }
            } catch (CoreException e) {
                e.printStackTrace();  // log real en producción
            }
        }

        return WorkspaceMetrics.builder()
        		.name(root.getLocation().lastSegment())
        		.analysisDate(LocalDate.now())
        		.projects(projects)
        		.build();
    }
    
    private ProjectMetrics buildProject(IProject project) throws CoreException {
        List<ClassMetrics> classes = new ArrayList<>();
        IJavaProject jProject = JavaCore.create(project);

        for (IPackageFragment pkg : jProject.getPackageFragments()) {
            if (!pkg.containsJavaResources()) continue;

            for (ICompilationUnit icu : pkg.getCompilationUnits()) {
                classes.add(buildClass(icu));
            }
        }

        return ProjectMetrics.builder()
                .name(project.getName())
                .classes(classes)
                .build();
    }
    
    private ClassMetrics buildClass(ICompilationUnit icu) {
        CompilationUnit cu = parseUnit(icu);

        List<MethodMetrics> methods = new ArrayList<>();

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                int currentCc  = ComplexityAnalyzer.compute(node);
                int currentLoc = loc(node);
                try {
                    methods.add(
                        extractor.analyseAndPlan(icu, cu, node, currentCc, currentLoc)
                    );
                } catch (CoreException | IOException e) {
                    e.printStackTrace();
                }
                return false;   // no visita los hijos de un método
            }
        });

        // Agregados LOC antes vs después
        int currentLocTotal    = sum(methods, LocStats::getCurrentLoc);
        int refactoredLocTotal = sum(methods, LocStats::getRefactoredLoc);

        return ClassMetrics.builder()
                .name(icu.getElementName().replace(".java", ""))
                .currentLoc(currentLocTotal)
                .refactoredLoc(refactoredLocTotal)
                .methods(methods)
                .build();
    }

    /** Parsea la unidad usando los ajustes por defecto del proyecto. */
    private CompilationUnit parseUnit(ICompilationUnit icu) {
        ASTParser parser = ASTParser.newParser(AST.JLS21);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(icu);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null);
    }

    private int loc(ASTNode node) {
        CompilationUnit cu = (CompilationUnit) node.getRoot();
        int start = cu.getLineNumber(node.getStartPosition());
        int end   = cu.getLineNumber(node.getStartPosition() + node.getLength());
        return Math.max(0, end - start + 1);
    }

    private static <T> int sum(List<T> list, ToIntFunction<T> mapper) {
        return list.stream().mapToInt(mapper).sum();
    }
}
