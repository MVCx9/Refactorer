package main.builder;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;

public class FileAnalysis {

	private final IFile file;
    private final ICompilationUnit icu;
    private final List<ClassAnalysis> classes;

    private FileAnalysis(IFile file, ICompilationUnit icu, List<ClassAnalysis> classes) {
        this.file = file;
        this.icu = icu;
        this.classes = List.copyOf(classes);
    }

    public static FileAnalysis of(IFile file, ICompilationUnit icu, List<ClassAnalysis> classes) {
        return new FileAnalysis(file, icu, classes);
    }

    public static FileAnalysis empty(IFile file) {
        return new FileAnalysis(file, null, List.of());
    }

    public IFile file() { return file; }
    public ICompilationUnit icu() { return icu; }
    public List<ClassAnalysis> classes() { return Collections.unmodifiableList(classes); }
}
