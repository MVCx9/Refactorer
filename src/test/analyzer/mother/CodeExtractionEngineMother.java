package test.analyzer.mother;

import main.refactor.CodeExtractionEngine;

public final class CodeExtractionEngineMother {
    private CodeExtractionEngineMother() {}
    public static CodeExtractionEngine mockEngine() {
        return org.mockito.Mockito.mock(CodeExtractionEngine.class);
    }
}
