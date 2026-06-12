package main.refactor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import main.common.error.ModifyFilesException;
import main.common.utils.Utils;
import main.model.clazz.ClassMetrics;

/**
 * Writes a planned source (either the refactored or the original code) to the
 * real workspace file backing a {@link ClassMetrics}.
 * <p>
 * The target file is located by the <b>workspace-relative path</b> captured at
 * analysis time ({@link ClassMetrics#getPath()}). That path includes the
 * project and source-folder segments, so it unambiguously identifies a single
 * file even when several classes share the same simple name across different
 * packages or projects. Resolving by simple file name is intentionally avoided
 * because it cannot disambiguate those cases.
 * </p>
 */
public final class RefactorApplier {

	private RefactorApplier() {
		// utility class
	}

	/**
	 * Applies the given source to the file identified by {@code classMetrics}.
	 *
	 * @param classMetrics the class whose backing file must be rewritten
	 * @param source       the full source to write; ignored when {@code null} or
	 *                     empty
	 * @throws ModifyFilesException if the file cannot be located or written
	 */
	public static void apply(ClassMetrics classMetrics, String source) {
		if (classMetrics == null || source == null || source.isEmpty()) {
			return;
		}

		ICompilationUnit unit = resolveCompilationUnit(classMetrics);
		if (unit == null) {
			throw new ModifyFilesException(
					"Could not locate the source file for class '" + classMetrics.getName()
							+ "' (path: " + classMetrics.getPath() + ")");
		}

		writeSource(unit, Utils.formatJava(source));
	}

	/**
	 * Resolves the {@link ICompilationUnit} backing the class, preferring the
	 * unique workspace-relative path and falling back to the absolute file-system
	 * location for backward compatibility with older stored analyses.
	 */
	static ICompilationUnit resolveCompilationUnit(ClassMetrics classMetrics) {
		String path = classMetrics.getPath();
		ICompilationUnit byWorkspacePath = findByWorkspacePath(path);
		return byWorkspacePath != null ? byWorkspacePath : findByFileSystemLocation(path);
	}

	private static ICompilationUnit findByWorkspacePath(String path) {
		if (path == null || path.isBlank()) {
			return null;
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(Path.fromPortableString(path));
		return toCompilationUnit(resource);
	}

	private static ICompilationUnit findByFileSystemLocation(String path) {
		if (path == null || path.isBlank()) {
			return null;
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath location = Path.fromOSString(path);
		IFile file = root.getFileForLocation(location);
		return toCompilationUnit(file);
	}

	private static ICompilationUnit toCompilationUnit(IResource resource) {
		if (!(resource instanceof IFile file) || !file.exists()) {
			return null;
		}
		IJavaElement element = JavaCore.create(file);
		return (element instanceof ICompilationUnit unit) ? unit : null;
	}

	private static void writeSource(ICompilationUnit unit, String source) {
		try {
			unit.becomeWorkingCopy(null);
			unit.getBuffer().setContents(source);
			unit.commitWorkingCopy(true, null);
		} catch (Exception e) {
			throw new ModifyFilesException("Error writing source to " + unit.getElementName(), e);
		} finally {
			try {
				unit.discardWorkingCopy();
			} catch (Exception ignore) {
				// A failed discard must not mask the outcome of the write above.
			}
		}
	}
}
