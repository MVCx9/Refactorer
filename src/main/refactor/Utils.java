package main.refactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import main.neo.cem.Utils.MethodDeclarationFinderVisitor;

public class Utils {

	public static <T> List<T> asImmutable(List<T> list) {
		return list == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(list));
	}

	public static <T> List<T> reverse(List<T> list) {
		List<T> copy = new ArrayList<>(list);
		Collections.reverse(copy);
		return Collections.unmodifiableList(copy);
	}
}
