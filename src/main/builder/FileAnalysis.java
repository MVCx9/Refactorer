package main.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;

public class FileAnalysis {

	private final IFile file;
	private final ICompilationUnit icu;
	private final ClassAnalysis classes;

	private FileAnalysis(IFile file, ICompilationUnit icu, ClassAnalysis classes) {
		this.file = file;
		this.icu = icu;
		this.classes = classes;
	}

	public static FileAnalysis of(IFile file, ICompilationUnit icu, ClassAnalysis classes) {
		return new FileAnalysis(file, icu, classes);
	}

	public static FileAnalysis empty(IFile file) {
		return new FileAnalysis(file, null, null);
	}

	public IFile getFile() {
		return file;
	}

	public ICompilationUnit getIcu() {
		return icu;
	}

	public ClassAnalysis getClasses() {
		return classes;
	}
}
