package test.analyzer.mother;

import org.eclipse.jdt.core.ICompilationUnit;

public final class ICompilationUnitMother {
    private ICompilationUnitMother() {}
    public static ICompilationUnit any() {
        return org.mockito.Mockito.mock(ICompilationUnit.class);
    }
}
