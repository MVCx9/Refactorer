package main.neo.cem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureProcessor;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.text.edits.TextEdit;

import main.neo.Constants;

/**
 * Cognitive complexity utilities using JDT
 */
public class Utils {
	/**
	 * Temporal name used by the oracle when testing if a code extraction into a new
	 * method is feasible.
	 */
	private final static String TEMPORAL_NAME_FOR_EXTRACTED_METHOD = "temporalMethodUnderTest";

	/**
	 * Create a {@link org.eclipse.jdt.core.dom.CompilationUnit CompilationUnit}
	 * from the path of a file in the workspace.
	 * 
	 * @param pathToFileInWorkspace relative file path in the workspace.
	 * @return The {@link org.eclipse.jdt.core.dom.CompilationUnit CompilationUnit}
	 *         associated to the file.
	 */
	public static CompilationUnit createCompilationUnitFromFileInWorkspace(String pathToFileInWorkspace) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath path = Path.fromOSString(pathToFileInWorkspace);
		IFile file = workspace.getRoot().getFile(path);
		ICompilationUnit element = JavaCore.createCompilationUnitFrom(file);
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setBindingsRecovery(true);
		parser.setSource(element);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

		return astRoot;
	}

	/**
	 * Create a {@link org.eclipse.jdt.core.dom.CompilationUnit CompilationUnit}
	 * from the path of a file in the system.
	 * 
	 * @param pathToFile absolute file path in the system.
	 * @return The {@link org.eclipse.jdt.core.dom.CompilationUnit CompilationUnit}
	 *         associated to the file or null if problems when reading giving file.
	 */
	public static CompilationUnit createCompilationUnitFromFile(String pathToFile) {
		String sourceCode;
		CompilationUnit astRoot = null;

		try {
			sourceCode = new String(Files.readAllBytes(Paths.get(pathToFile)));
			ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
			parser.setSource(sourceCode.toCharArray());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);

			astRoot = (CompilationUnit) parser.createAST(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return astRoot;
	}

	/**
	 * Check if a node can be extracted as a new method in the same compilation
	 * unit.
	 * 
	 * @param compilationUnit The compilation unit under processing.
	 * @param node            to check.
	 * @return Metrics of the code extraction: if the extraction is feasible, the
	 *         reason why the extraction would fail, the length of the extracted
	 *         code, ...
	 */
	public static CodeExtractionMetrics checkNodeExtraction(CompilationUnit compilationUnit, ASTNode node) {
		CodeExtractionMetrics result;

		result = Utils.extractCode(compilationUnit, node.getStartPosition(), node.getLength(),
				TEMPORAL_NAME_FOR_EXTRACTED_METHOD, true);

		return result;
	}

	/**
	 * Check if existing code between two nodes, including both of them, can be
	 * extracted as a new method in the same compilation unit.
	 * 
	 * @param compilationUnit The compilation unit under processing.
	 * @param nodeA           Node used as the starting point of the code to be
	 *                        extracted.
	 * @param nodeB           Node used as the end point of the code to be
	 *                        extracted.
	 * @return Metrics of the code extraction: if the extraction is feasible, the
	 *         reason why the extraction would failed, the length of the extracted
	 *         code, ...
	 */
	public static CodeExtractionMetrics checkCodeExtractionBetweenTwoNodes(CompilationUnit compilationUnit,
			ASTNode nodeA, ASTNode nodeB) {
		CodeExtractionMetrics result;
		int startPosition = nodeA.getStartPosition();
		int endPosition = nodeB.getStartPosition() + nodeB.getLength();
		int length = endPosition - startPosition;

		result = Utils.extractCode(compilationUnit, nodeA.getStartPosition(), length,
				TEMPORAL_NAME_FOR_EXTRACTED_METHOD, true);

		return result;
	}

	/**
	 * Extract existing code between two nodes, including both of them, as a new
	 * method in the same compilation unit.
	 * 
	 * @param compilationUnit     The compilation unit under processing.
	 * @param nodeA               Node used as the starting point of the code to be
	 *                            extracted.
	 * @param nodeB               Node used as the end point of the code to be
	 *                            extracted.
	 * @param extractedMethodName Name of the extracted method.
	 * @param simulation          true to just check if the refactoring would be
	 *                            valid. If true, the refactoring will be undone
	 *                            (useful for testing).
	 * @return Metrics of the code extraction: if the extraction was feasible,
	 *         applied, the reason why the extraction failed, the length of the
	 *         extracted code, ...
	 */
	public static CodeExtractionMetrics extractCodeBetweenTwoNodes(CompilationUnit compilationUnit, ASTNode nodeA,
			ASTNode nodeB, String extractedMethodName, boolean simulation) {
		CodeExtractionMetrics result;
		int startPosition = nodeA.getStartPosition();
		int endPosition = nodeB.getStartPosition() + nodeB.getLength();
		int length = endPosition - startPosition;

		result = Utils.extractCode(compilationUnit, nodeA.getStartPosition(), length, extractedMethodName, simulation);

		return result;
	}

	/**
	 * Get {@link org.eclipse.ltk.core.refactoring.Change Change} to apply to
	 * refactor method name
	 * 
	 * @param node    The method to be refactored
	 * @param newName The new name of the method
	 * @return Null if the the refactor cannot be applied or the
	 *         {@link org.eclipse.ltk.core.refactoring.Change Change} to be applied
	 *         otherwise
	 */
	public static Change changeMethodName(MethodDeclaration node, String newName) {
		ChangeSignatureProcessor changeSignatureProcessor = null;
		Change change = null;
		try {
			changeSignatureProcessor = new ChangeSignatureProcessor((IMethod) node.resolveBinding().getJavaElement());

			changeSignatureProcessor.setNewMethodName(newName);

			CheckConditionsContext context = new CheckConditionsContext();
			context.add(new ValidateEditChecker(null));
			context.add(new ResourceChangeChecker());

			IProgressMonitor monitor = new NullProgressMonitor();

			RefactoringStatus status = changeSignatureProcessor.checkInitialConditions(monitor);
			if (status.isOK()) {
				status = changeSignatureProcessor.checkFinalConditions(monitor, context);
				if (status.isOK()) {
					change = changeSignatureProcessor.createChange(monitor);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return change;
	}

	/**
	 * Refactor a compilation unit extracting the given source code as a new method.
	 * 
	 * @param compilationUnit     The compilation unit under processing
	 * @param selectionStart      The offset of the start of the code to refactor.
	 * @param selectionLength     The length of the code to refactor from the
	 *                            selection start.
	 * @param extractedMethodName Name of the extracted method.
	 * @param simulation          true to just check if the refactoring would be
	 *                            valid. If true, the refactoring will be undone
	 *                            (useful for testing).
	 * @return Metrics of the code extraction: if the extraction was feasible,
	 *         applied, the reason why the extraction failed, the length of the
	 *         extracted code, ...
	 * @throws CoreException
	 */
	public static CodeExtractionMetrics extractCode(CompilationUnit compilationUnit, int selectionStart,
			int selectionLength, String extractedMethodName, boolean simulation) {
		CodeExtractionMetrics result;
		List<Change> changes = new ArrayList<Change>();
		List<Change> undoChanges = new ArrayList<Change>();
		boolean refactoringApplied = false;
		boolean feasible = true;
		int numberOfExtractedLinesOfCode = 0, numberOfParametersInExtractedMethod = 0;
		IProgressMonitor npm = new NullProgressMonitor();
		String resultOfRefactoring = "";
		boolean compilationErrors = false;

		try {
			// Metrics independent of simulation path (need original CU)
			numberOfExtractedLinesOfCode = numberOfLinesOfCode(compilationUnit, selectionStart, selectionLength);

			if (simulation) {
				// Pure in-memory simulation using a working copy so file on disk is untouched
				ICompilationUnit icu = (ICompilationUnit) compilationUnit.getJavaElement();
				ICompilationUnit wc = icu.getWorkingCopy(null);
				try {
					ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(wc, selectionStart,
							selectionLength);
					refactoring.setMethodName(extractedMethodName);
					refactoring.setReplaceDuplicates(false);

					RefactoringStatus status = refactoring.checkInitialConditions(npm);
					if (!status.isOK()) {
						feasible = false;
						resultOfRefactoring = status.getEntryAt(0).getMessage();
					} else {
						status = refactoring.checkFinalConditions(npm);
						if (!status.isOK()) {
							feasible = false;
							resultOfRefactoring = status.getEntryAt(0).getMessage();
						} else {
							resultOfRefactoring = "OK";
							numberOfParametersInExtractedMethod = refactoring.getParameterInfos().size();
							Change change = refactoring.createChange(npm);
							changes.add(change); // for traceability only
							TextEdit edit = extractPrimaryTextEdit(change);
							if (edit == null) {
								feasible = false;
								resultOfRefactoring = "Cannot locate primary TextEdit for refactoring";
							} else {
								// Apply edit to a temporary Document (do not touch working copy buffer to
								// preserve original offsets for other simulations)
								Document doc = new Document(wc.getSource());
								try {
									edit.apply(doc);
									// Optionally we could parse doc.get() if we needed the post-state AST for
									// metrics
									// but current callers only need feasibility & parameter count.
									refactoringApplied = true; // conceptually applicable
								} catch (Exception te) {
									feasible = false;
									resultOfRefactoring = "Simulation edit failed: " + te.getMessage();
								}
							}
						}
					}
				} finally {
					if (wc != null) {
						wc.discardWorkingCopy();
					}
				}
			} else {
				// Original behavior: applies change to file (then possibly undone by caller)
				ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(compilationUnit, selectionStart,
						selectionLength);
				refactoring.setMethodName(extractedMethodName);
				RefactoringStatus status = refactoring.checkInitialConditions(npm);
				if (status.isOK()) {
					status = refactoring.checkFinalConditions(npm);
					if (status.isOK()) {
						resultOfRefactoring = "OK";
						numberOfParametersInExtractedMethod = refactoring.getParameterInfos().size();
						refactoring.setReplaceDuplicates(false);
						Change c = refactoring.createChange(npm);
						Change undo = c.perform(npm);
						CompilationUnit compilationUnitAfterRefactoring = createCompilationUnitFromFileInWorkspace(
								compilationUnit.getJavaElement().getPath().toOSString());
						compilationErrors = builtWithCompilationErrors(compilationUnitAfterRefactoring);
						if (compilationErrors) {
							resultOfRefactoring = "Compilation unit does not compile after method extraction.";
							feasible = false;
							undo.perform(npm);
						} else {
							changes.add(c);
							undoChanges.add(undo);
							refactoringApplied = true;
						}
					} else {
						feasible = false;
						resultOfRefactoring = status.getEntryAt(0).getMessage();
					}
				} else {
					feasible = false;
					resultOfRefactoring = status.getEntryAt(0).getMessage();
				}
			}
		} catch (CoreException e) {
			feasible = false;
			resultOfRefactoring = "Extract Method failed: " + e.getMessage();
		}

		result = new CodeExtractionMetrics(feasible, resultOfRefactoring, refactoringApplied,
				numberOfExtractedLinesOfCode, numberOfParametersInExtractedMethod, changes, undoChanges);

		return result;
	}

	public static CodeExtractionMetrics extractCode(CompilationUnit compilationUnit, ICompilationUnit icuWorkingCopy,
			int selectionStart, int selectionLength, String extractedMethodName, boolean simulation) {
		CodeExtractionMetrics result;
		List<Change> changes = new ArrayList<Change>();
		List<Change> undoChanges = new ArrayList<Change>(); // no real undo in pure in-memory path
		boolean refactoringApplied = false;
		boolean feasible = true;
		int numberOfExtractedLinesOfCode = 0, numberOfParametersInExtractedMethod = 0;
		IProgressMonitor npm = new NullProgressMonitor();
		String resultOfRefactoring = "";
		boolean compilationErrors = false;
		CompilationUnit refactoredCompilationUnit = null;

		try {
			// Ensure selection is within current working copy source
			String currentSource = icuWorkingCopy.getSource();
			// Basic guard
			if (selectionStart < 0 || selectionStart + selectionLength > currentSource.length()) {
				feasible = false;
				resultOfRefactoring = "Invalid selection range for current source";
				return new CodeExtractionMetrics(feasible, resultOfRefactoring, refactoringApplied,
						numberOfExtractedLinesOfCode, numberOfParametersInExtractedMethod, changes, undoChanges,
						compilationUnit);
			}

			// Build refactoring on working copy (its buffer content is our truth)
			ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(icuWorkingCopy, selectionStart,
					selectionLength);
			refactoring.setMethodName(extractedMethodName);
			refactoring.setReplaceDuplicates(false);

			RefactoringStatus status = refactoring.checkInitialConditions(npm);
			if (!status.isOK()) {
				feasible = false;
				resultOfRefactoring = status.getEntryAt(0).getMessage();
				return new CodeExtractionMetrics(feasible, resultOfRefactoring, refactoringApplied,
						numberOfExtractedLinesOfCode, numberOfParametersInExtractedMethod, changes, undoChanges,
						compilationUnit);
			}
			status = refactoring.checkFinalConditions(npm);
			if (!status.isOK()) {
				feasible = false;
				resultOfRefactoring = status.getEntryAt(0).getMessage();
				return new CodeExtractionMetrics(feasible, resultOfRefactoring, refactoringApplied,
						numberOfExtractedLinesOfCode, numberOfParametersInExtractedMethod, changes, undoChanges,
						compilationUnit);
			}
			resultOfRefactoring = "OK";

			// Metrics prior to applying edit
			numberOfExtractedLinesOfCode = numberOfLinesOfCode(compilationUnit, selectionStart, selectionLength);
			numberOfParametersInExtractedMethod = refactoring.getParameterInfos().size();

			// Create change (do NOT perform on workspace)
			Change change = refactoring.createChange(npm);
			changes.add(change);

			TextEdit edit = extractPrimaryTextEdit(change);
			if (edit == null) {
				feasible = false;
				resultOfRefactoring = "Cannot locate primary TextEdit for refactoring";
				return new CodeExtractionMetrics(feasible, resultOfRefactoring, refactoringApplied,
						numberOfExtractedLinesOfCode, numberOfParametersInExtractedMethod, changes, undoChanges,
						compilationUnit);
			}

			// Apply edit in-memory
			Document doc = new Document(currentSource);
			edit.apply(doc); // may throw exceptions if edit invalid
			String newSource = doc.get();

			// Re-parse updated source through working copy so further extractions refer to
			// updated offsets
			icuWorkingCopy.getBuffer().setContents(newSource);
			// Reconcile to refresh internal state (bindings if needed)
			icuWorkingCopy.reconcile(ICompilationUnit.NO_AST, false, null, npm);

			ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(icuWorkingCopy); // parse from working copy with updated buffer
			refactoredCompilationUnit = (CompilationUnit) parser.createAST(null);

			// Compilation problem check
			compilationErrors = builtWithCompilationErrors(refactoredCompilationUnit);
			if (compilationErrors) {
				// revert buffer
				icuWorkingCopy.getBuffer().setContents(currentSource);
				icuWorkingCopy.reconcile(ICompilationUnit.NO_AST, false, null, npm);
				feasible = false;
				resultOfRefactoring = "Compilation unit does not compile after method extraction.";
				refactoredCompilationUnit = compilationUnit; // keep old AST
			} else {
				refactoringApplied = true;
				// Persist to disk only if simulation == false
				if (!simulation) {
					icuWorkingCopy.commitWorkingCopy(true, npm);
				}
			}

		} catch (Exception e) {
			feasible = false;
			resultOfRefactoring = "Extract Method failed: " + e.getMessage();
			refactoredCompilationUnit = compilationUnit;
		}

		result = new CodeExtractionMetrics(feasible, resultOfRefactoring, refactoringApplied,
				numberOfExtractedLinesOfCode, numberOfParametersInExtractedMethod, changes, undoChanges,
				refactoredCompilationUnit);

		return result;
	}

	// Helper to obtain the primary TextEdit from a Change tree
	private static TextEdit extractPrimaryTextEdit(Change change) {
		if (change == null)
			return null;
		if (change instanceof TextFileChange) {
			return ((TextFileChange) change).getEdit();
		}
		if (change instanceof CompositeChange) {
			Change[] children = ((CompositeChange) change).getChildren();
			for (Change c : children) {
				TextEdit te = extractPrimaryTextEdit(c);
				if (te != null)
					return te;
			}
		}
		return null;
	}

	/**
	 * Check if the compilation unit has compilation errors.
	 * 
	 * @param compilationUnit The compilation unit under processing.
	 * @return true if the compilation unit has compilation errors.
	 * @throws CoreException
	 */
	public static boolean builtWithCompilationErrors(CompilationUnit compilationUnit) throws CoreException {
		IProgressMonitor npm = new NullProgressMonitor();
		boolean error = false;
		IProblem problems[] = compilationUnit.getProblems();

		int index = 1;
		while (!error && index <= problems.length) {
			error = problems[index - 1].isError();
			index++;
		}

		return error;
	}

	public static String getCompilationUnitProblems(CompilationUnit compilationUnit) {
		StringJoiner result = new StringJoiner(System.lineSeparator());

		for (IProblem p : compilationUnit.getProblems()) {
			result.add("Error? " + p.isError() + ": " + p.toString());
		}

		return result.toString();
	}

	/**
	 * Return the node in the given compilation unit at the given line of code and
	 * offset.
	 * 
	 * @param compilationUnit The compilation unit under processing.
	 * @param startLine       Line of code where the node is located in the
	 *                        compilation unit.
	 * @param startOffset     Character index into the startLine where the node is
	 *                        located in the compilation unit.
	 * @return the innermost node that fully contains the selection.
	 */
	public static ASTNode findNode(CompilationUnit compilationUnit, int startLine, int startOffset) {
		NodeFinder finder = new NodeFinder(compilationUnit, compilationUnit.getPosition(startLine, startOffset), 0);
		ASTNode node = finder.getCoveringNode();

		return node;
	}

	/**
	 * Compute the number of lines of code in the compilation unit for a given node
	 * of the AST.
	 * 
	 * @param compilationUnit The compilation unit under processing.
	 * @param node            Node of the AST for which the extension in lines of
	 *                        code will be computed.
	 * @return The number of lines of code of the node in the compilation unit.
	 */
	public static int numberOfLinesOfCode(CompilationUnit compilationUnit, ASTNode node) {
		int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		int endLineNumber = compilationUnit.getLineNumber(node.getStartPosition() + node.getLength() - 1);
		int result;

		result = (endLineNumber - startLineNumber) + 1;

		return result;
	}

	/**
	 * Compute the number of lines for the given code in a compilation unit.
	 * 
	 * @param compilationUnit The compilation unit under processing.
	 * @param startPosition   The offset of the start of the code.
	 * @param length          The length of the code from the start position.
	 * @return The number of lines of code in the compilation unit.
	 */
	public static int numberOfLinesOfCode(CompilationUnit compilationUnit, int startPosition, int length) {
		int startLineNumber = compilationUnit.getLineNumber(startPosition);
		int endLineNumber = compilationUnit.getLineNumber(startPosition + length - 1);
		int result = (endLineNumber - startLineNumber) + 1;

		return result;
	}

	/**
	 * Given a method, return the type of its parameters in the signature.
	 * 
	 * @param method The method to obtain its signature
	 * @return The type of the params of the method signature
	 */
	public static String[] getTypesInSignature(MethodDeclaration method) {
		String result[] = new String[method.parameters().size()];

		for (int i = 0; i < method.parameters().size(); i++) {
			result[i] = ((SingleVariableDeclaration) method.parameters().get(i)).getType().toString();
		}

		return result;
	}

	/**
	 * Get siblings of a given node
	 * 
	 * @param node
	 * @return List of siblings (excluding itself)
	 */
	public static List<ASTNode> getSiblings(ASTNode node) {
		List<ASTNode> siblings = new ArrayList<ASTNode>();
		ASTNode parent = node.getParent();

		if (parent != null) {
			List<?> list = parent.structuralPropertiesForType();
			for (int i = 0; i < list.size(); i++) {
				Object sibling = parent.getStructuralProperty((StructuralPropertyDescriptor) list.get(i));
				if (sibling instanceof ASTNode) {
					if (!sibling.equals(node)) {
						siblings.add((ASTNode) sibling);
					}
				}
				// If node is not an ASTNode (it is a Block, for example), sibling can contain a
				// List of ASTNodes (for instance, a List of Statement in a Block)
				else if (sibling instanceof List<?>) {
					for (Object s : (List<?>) sibling) {
						if (!s.equals(node)) {
							siblings.add((ASTNode) s);
						}
					}
				}
			}
		}

		return siblings;
	}

	/**
	 * Get the integer value of the given property of a given node
	 * 
	 * @param node
	 * @param property
	 * @return Value of the given property of the node
	 */
	public static int getIntegerPropertyOfNode(ASTNode node, String property) {
		Integer result = (Integer) node.getProperty(property);
		if (result == null) {
			result = 0;
		}
		return result;
	}

	/**
	 * Get siblings found after the node (right side)
	 * 
	 * @param node
	 * @return List of siblings
	 */
	public static List<ASTNode> getRightSiblings(ASTNode node) {
		List<ASTNode> siblings = new ArrayList<ASTNode>();
		ASTNode parent = node.getParent();
		boolean found = false;

		if (parent != null) {
			List<?> list = parent.structuralPropertiesForType();
			for (int i = 0; i < list.size(); i++) {
				Object sibling = parent.getStructuralProperty((StructuralPropertyDescriptor) list.get(i));
				if (sibling instanceof ASTNode) {
					if (sibling.equals(node)) {
						found = true;
					} else {
						if (found) {
							siblings.add((ASTNode) sibling);
						}
					}
				}
				// If node is not an ASTNode (it is a Block, for example), sibling can contain a
				// List of ASTNodes (for instance, a List of Statement in a Block)
				else if (sibling instanceof List<?>) {
					for (Object s : (List<?>) sibling) {
						if (s.equals(node)) {
							found = true;
						} else {
							if (found) {
								siblings.add((ASTNode) s);
							}
						}

					}
				}
			}
		}

		return siblings;
	}

	/**
	 * Get children of a given node
	 * 
	 * @param node
	 * @return List of children
	 */
	public static List<ASTNode> getChildren(ASTNode node) {
		List<ASTNode> children = new ArrayList<ASTNode>();
		List<?> list = node.structuralPropertiesForType();
		for (int i = 0; i < list.size(); i++) {
			Object child = node.getStructuralProperty((StructuralPropertyDescriptor) list.get(i));
			if (child instanceof ASTNode) {
				children.add((ASTNode) child);
			}
		}
		return children;
	}

	/**
	 * Perform undo changes. These changes are undo from the ending of the list to
	 * the beginning.
	 * 
	 * @param changes to undo.
	 */
	public static void undoChanges(List<Change> changes) {
		for (int i = changes.size() - 1; i >= 0; i--) {
			try {
				changes.get(i).perform(null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Perform changes. These changes are performed from the beginning of the list
	 * to the ending.
	 * 
	 * @param changes to undo.
	 */
	public static void performChanges(List<Change> changes) {
		for (int i = 0; i < changes.size(); i++) {
			try {
				changes.get(i).perform(null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get all ancestors, of the given node, contributing to cognitive complexity.
	 * 
	 * @param node
	 * @return Ancestors, of the given node, contributing to cognitive complexity.
	 */
	public static List<ASTNode> getAllAncestorContributingToComplexity(ASTNode node) {
		List<ASTNode> ancestors = new ArrayList<ASTNode>();

		ASTNode parent = node.getParent();

		// Note that a method could declare more methods in an anonymous class. Skip
		// those cases
		while (parent != null && !(parent instanceof MethodDeclaration
				&& !(parent.getParent() instanceof AnonymousClassDeclaration))) {

			if (parent.getProperty(Constants.CONTRIBUTION_TO_COMPLEXITY) != null) {
				ancestors.add(parent);
			}

			parent = parent.getParent();
		}

		return ancestors;
	}

	public static void fillAncestorsAndChildren(List<ASTNode> nodes, Map<ASTNode, List<ASTNode>> ancestors,
			Map<ASTNode, List<ASTNode>> children) {
		for (ASTNode node : nodes) {
			List<ASTNode> listAncestors = Utils.getAllAncestorContributingToComplexity(node);
			ancestors.put(node, listAncestors);
			fillChildren(node, listAncestors, children);

		}
	}

	private static void fillChildren(ASTNode node, List<ASTNode> listAncestors, Map<ASTNode, List<ASTNode>> children) {
		for (ASTNode ancestor : listAncestors) {
			List<ASTNode> c = children.get(ancestor);
			if (c == null) {
				c = new ArrayList<>();
				children.put(ancestor, c);
			}
			c.add(node);
		}
	}

	/**
	 * Get the method declaration associated to the given ASTNode It assumes the
	 * given node belongs to a method declaration If not, for instance when node is
	 * an import declaration, this method will have an unexpected behavior
	 * 
	 * @return Node associated to the method declaration of the given node
	 */
	public static MethodDeclaration getMethodDeclaration(ASTNode node) {
		ASTNode result = node;

		while (!(result instanceof MethodDeclaration)) {
			result = result.getParent();

			// A method could declare more methods in an anonymous class. Skip those cases
			if (result instanceof MethodDeclaration) {
				if (result.getParent() instanceof AnonymousClassDeclaration) {
					result = result.getParent();
				}
			}
		}

		return (MethodDeclaration) result;
	}

	/**
	 * Compute the nesting component of cognitive complexity of the given node
	 * 
	 * @param node
	 * @return The nesting component of cognitive complexity of the node
	 */
	public static int computeNesting(ASTNode node) {
		int nesting = 0;
		ASTNode current = node;
		ASTNode child = null;
		ASTNode root = getMethodDeclaration(node);

		while (current != null && !(current.equals(root))) {
			child = current;
			current = current.getParent();

			switch (current.getNodeType()) {
			case ASTNode.FOR_STATEMENT:
			case ASTNode.ENHANCED_FOR_STATEMENT:
			case ASTNode.WHILE_STATEMENT:
			case ASTNode.DO_STATEMENT:
			case ASTNode.CATCH_CLAUSE:
			case ASTNode.SWITCH_STATEMENT:
			case ASTNode.LAMBDA_EXPRESSION:
			case ASTNode.CONDITIONAL_EXPRESSION:
				nesting++;
				break;
			case ASTNode.METHOD_DECLARATION:
				if (!current.equals(root))
					nesting++;
				break;
			case ASTNode.IF_STATEMENT:
				if (child.getLocationInParent().equals(IfStatement.THEN_STATEMENT_PROPERTY)) {
					nesting++;
				} else if (child.getLocationInParent().equals(IfStatement.ELSE_STATEMENT_PROPERTY)) {
					if (!(child instanceof IfStatement)) {
						nesting++;
					}
				}
				break;
			}
		}

		return nesting;
	}

	/**
	 * Compute and annotate in the AST of a method the accumulated inherent and
	 * nesting components of cognitive complexity and the number of nodes with
	 * contributions to the nesting component.
	 * 
	 * @param ast The AST of the method under processing.
	 * @return The total complexity of the method
	 */
	public static int computeAndAnnotateAccumulativeCognitiveComplexity(MethodDeclaration ast) {
		int result;

		// NEW: limpiar propiedades previas en todo el subárbol para evitar acumulación
		// entre invocaciones sucesivas
		resetCognitiveComplexityProperties(ast);

		// Pre-pass: annotate base contributions on nodes
		annotateBaseCognitiveComplexityContributions(ast);

		computeAllComponentsOfCognitiveComplexity(ast);

		result = (int) ast.getProperty(Constants.ACCUMULATED_COMPLEXITY);

		return result;
	}

	/**
	 * Recorre recursivamente el subárbol y elimina (pone a null) todas las
	 * propiedades usadas en el cálculo de complejidad cognitiva para evitar que
	 * llamadas repetidas sumen valores antiguos.
	 */
	private static void resetCognitiveComplexityProperties(ASTNode node) {
		if (node == null)
			return;
		
		for (String p : Constants.PROPERTIES) {
			node.setProperty(p, null);
		}
		// Descender
		List<?> structural = node.structuralPropertiesForType();
		for (Object spdObj : structural) {
			if (!(spdObj instanceof org.eclipse.jdt.core.dom.StructuralPropertyDescriptor))
				continue;
			var spd = (org.eclipse.jdt.core.dom.StructuralPropertyDescriptor) spdObj;
			Object child = node.getStructuralProperty(spd);
			if (child instanceof ASTNode) {
				resetCognitiveComplexityProperties((ASTNode) child);
			} else if (child instanceof List<?>) {
				for (Object o : (List<?>) child) {
					if (o instanceof ASTNode) {
						resetCognitiveComplexityProperties((ASTNode) o);
					}
				}
			}
		}
	}

	/**
	 * Annotate per-node base contributions to cognitive complexity so they can be
	 * accumulated afterwards. This sets inherent contributions and stores nesting
	 * components separately (and also folds nesting into the main contribution so
	 * ACCUMULATED_COMPLEXITY already reflects nesting for the method total).
	 */
	private static void annotateBaseCognitiveComplexityContributions(MethodDeclaration ast) {
		ast.accept(new ASTVisitor() {
			private final Deque<Boolean> switchFirstCase = new ArrayDeque<>();

			@Override
			public boolean visit(IfStatement node) {
				addContribution(node, 1, true);
				return true;
			}

			@Override
			public boolean visit(SwitchStatement node) {
				addContribution(node, 1, true);
				switchFirstCase.push(Boolean.TRUE);
				return true;
			}

			@Override
			public void endVisit(SwitchStatement node) {
				if (!switchFirstCase.isEmpty())
					switchFirstCase.pop();
			}

			@Override
			public boolean visit(SwitchCase node) {
				boolean first = !switchFirstCase.isEmpty() && switchFirstCase.peek();
				if (first) {
					switchFirstCase.pop();
					switchFirstCase.push(Boolean.FALSE);
				} else {
					addContribution(node, 1, false);
				}
				return true;
			}

			@Override
			public boolean visit(org.eclipse.jdt.core.dom.ForStatement node) {
				addContribution(node, 1, true);
				return true;
			}

			@Override
			public boolean visit(org.eclipse.jdt.core.dom.EnhancedForStatement node) {
				addContribution(node, 1, true);
				return true;
			}

			@Override
			public boolean visit(org.eclipse.jdt.core.dom.WhileStatement node) {
				addContribution(node, 1, true);
				return true;
			}

			@Override
			public boolean visit(org.eclipse.jdt.core.dom.DoStatement node) {
				addContribution(node, 1, true);
				return true;
			}

			@Override
			public boolean visit(TryStatement node) {
				addContribution(node, 1, true);
				return true;
			}

			@Override
			public boolean visit(ConditionalExpression node) {
				addContribution(node, 1, true);
				return true;
			}

			@Override
			public boolean visit(InfixExpression node) {
				int logicalOps = countLogicalOperators(node);
				if (logicalOps > 0) {
					addContribution(node, logicalOps, false);
					return false;
				}
				return true;
			}

			private int countLogicalOperators(InfixExpression node) {
				int count = 0;
				if (node.getOperator() == InfixExpression.Operator.CONDITIONAL_AND
						|| node.getOperator() == InfixExpression.Operator.CONDITIONAL_OR)
					count++;
				if (node.getLeftOperand() instanceof InfixExpression)
					count += countLogicalOperators((InfixExpression) node.getLeftOperand());
				if (node.getRightOperand() instanceof InfixExpression)
					count += countLogicalOperators((InfixExpression) node.getRightOperand());
				for (Object ext : node.extendedOperands())
					if (ext instanceof InfixExpression)
						count += countLogicalOperators((InfixExpression) ext);
				return count;
			}

			private void addContribution(ASTNode node, int delta, boolean addNesting) {
				int current = Utils.getIntegerPropertyOfNode(node, Constants.CONTRIBUTION_TO_COMPLEXITY);
				int updated = current + delta;
				if (addNesting) {
					int nesting = Utils.computeNesting(node);
					int curNest = Utils.getIntegerPropertyOfNode(node, Constants.CONTRIBUTION_TO_COMPLEXITY_BY_NESTING);
					node.setProperty(Constants.CONTRIBUTION_TO_COMPLEXITY_BY_NESTING, curNest + nesting);
					updated += nesting;
				}
				node.setProperty(Constants.CONTRIBUTION_TO_COMPLEXITY, updated);
			}
		});
	}

	/**
	 * Compute and annotate in the AST of a method the accumulated inherent and
	 * nesting components of cognitive complexity and the number of nodes with
	 * contributions to the nesting component.
	 * 
	 * @param ast The AST of the method under processing.
	 */
	private static void computeAllComponentsOfCognitiveComplexity(MethodDeclaration ast) {
		ast.accept(new ASTVisitor() {
			public void postVisit(ASTNode node) {
				accumulateProperties(node, Constants.CONTRIBUTION_TO_COMPLEXITY, Constants.ACCUMULATED_COMPLEXITY);
				accumulateProperties(node, Constants.CONTRIBUTION_TO_COMPLEXITY_BY_NESTING,
						Constants.ACCUMULATED_CONTRIBUTION_TO_COMPLEXITY_BY_NESTING);
				countNodesWithProperty(node,
						n -> Utils.getIntegerPropertyOfNode(n, Constants.CONTRIBUTION_TO_COMPLEXITY_BY_NESTING) > 0,
						Constants.ACCUMULATED_NUMBER_NESTING_COMPLEXITY_CONTRIBUTORS);

				int acc = Utils.getIntegerPropertyOfNode(node, Constants.ACCUMULATED_COMPLEXITY);
				int accn = Utils.getIntegerPropertyOfNode(node,
						Constants.ACCUMULATED_CONTRIBUTION_TO_COMPLEXITY_BY_NESTING);
				int mu = Utils.getIntegerPropertyOfNode(node,
						Constants.ACCUMULATED_NUMBER_NESTING_COMPLEXITY_CONTRIBUTORS);

				int iota = acc - accn;
				node.setProperty(Constants.ACCUMULATED_INHERENT_COMPLEXITY_COMPONENT, iota);

				int nesting = Utils.computeNesting(node);
				int nu = accn - mu * nesting;
				node.setProperty(Constants.ACCUMULATED_NESTING_COMPLEXITY_COMPONENT, nu);

				int ccNewExtractedMethod = iota + nu;
				node.setProperty(Constants.COMPLEXITY_WHEN_EXTRACTING, ccNewExtractedMethod);
			}

			private void countNodesWithProperty(ASTNode node, Predicate<ASTNode> predicate,
					String accumulatorProperty) {
				int accumulated = Utils.getIntegerPropertyOfNode(node, accumulatorProperty);
				if (predicate.test(node)) {
					accumulated++;
				}
				node.setProperty(accumulatorProperty, accumulated);

				ASTNode parent = node.getParent();
				if (parent != null) {
					int parentAccumulated = Utils.getIntegerPropertyOfNode(parent, accumulatorProperty);
					parent.setProperty(accumulatorProperty, accumulated + parentAccumulated);
				}
			}

			private void accumulateProperties(ASTNode node, String property, String accumulatorProperty) {
				int contribution = Utils.getIntegerPropertyOfNode(node, property);
				int accumulated = Utils.getIntegerPropertyOfNode(node, accumulatorProperty);
				accumulated += contribution;
				node.setProperty(accumulatorProperty, accumulated);

				ASTNode parent = node.getParent();
				if (parent != null) {
					int parentAccumulated = Utils.getIntegerPropertyOfNode(parent, accumulatorProperty);
					parent.setProperty(accumulatorProperty, accumulated + parentAccumulated);
				}
			}

		});

	}

	/**
	 * Get ancestor, of the given node, contributing to cognitive complexity.
	 * 
	 * @param node
	 * @return Ancestor, of the given node, contributing to cognitive complexity.
	 */
	public ASTNode getAncestorContributingToComplexity(ASTNode node) {
		ASTNode parent = node.getParent();

		// Note that a method could declare more methods in an anonymous class. Skip
		// those cases
		while (parent != null
				&& !(parent instanceof MethodDeclaration && !(parent.getParent() instanceof AnonymousClassDeclaration))
				&& parent.getProperty(Constants.CONTRIBUTION_TO_COMPLEXITY) == null) {
			parent = parent.getParent();
		}

		return parent;
	}

	/**
	 * Instantiate a personalized node finder using the given root node, the given
	 * start and the given length. The field {@code nodes} contains the sibling
	 * nodes in the code from the given start in the given length
	 * 
	 * @param root   the given root node
	 * @param start  the given start
	 * @param length the given length
	 */
	public static class NodeFinderVisitorForGivenSelection extends ASTVisitor {
		private List<ASTNode> nodes;
		private ASTNode parent;
		private int fStart;
		private int fEnd;
		private ASTNode fCoveringNode;
		private ASTNode fCoveredNode;

		public NodeFinderVisitorForGivenSelection(ASTNode root, int offset, int length) {
			super(true); // include Javadoc tags
			nodes = new ArrayList<ASTNode>();
			this.fStart = offset;
			this.fEnd = offset + length;
			root.accept(this);
		}

		public List<ASTNode> getNodes() {
			return nodes;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			int nodeStart = node.getStartPosition();
			int nodeEnd = nodeStart + node.getLength();

			if (this.fStart <= nodeStart && nodeEnd <= this.fEnd) {
				if (nodes.isEmpty()) {
					parent = node.getParent();
					nodes.add(node);
				} else if (node.getParent() == parent) {
					nodes.add(node);
				}
			}

			// The code below is the original code from NodeFinderVisitor
			if (nodeEnd < this.fStart || this.fEnd < nodeStart) {
				return false;
			}
			if (nodeStart <= this.fStart && this.fEnd <= nodeEnd) {
				this.fCoveringNode = node;
			}
			if (this.fStart <= nodeStart && nodeEnd <= this.fEnd) {
				if (this.fCoveringNode == node) { // nodeStart == fStart && nodeEnd == fEnd
					this.fCoveredNode = node;
					return true; // look further for node with same length as parent
				} else if (this.fCoveredNode == null) { // no better found
					this.fCoveredNode = node;
				}
				return false;
			}
			return true;
		}
	}

	/**
	 * Visitor for a method declaration in the AST
	 * 
	 * @param methodLookingFor  the name of the method to look for
	 * @param methodParameters  parameters of the method (its signature)
	 * @param found             true if the method declaration is in the AST
	 * @param methodDeclaration the method declaration node in the AST if found
	 */
	public static class MethodDeclarationFinderVisitor extends ASTVisitor {
		private String methodLookingFor;
		private List<SingleVariableDeclaration> methodParameters;
		private boolean found;
		private MethodDeclaration methodDeclaration;

		public MethodDeclarationFinderVisitor(ASTNode root, String methodName,
				List<SingleVariableDeclaration> parameters) {
			super(true); // include Javadoc tags
			methodLookingFor = methodName;
			methodParameters = parameters;
			methodDeclaration = null;
			found = false;
			root.accept(this);
		}

		public boolean found() {
			return this.found;
		}

		public MethodDeclaration getMethodDeclaration() {
			return methodDeclaration;
		}

		@Override
		public boolean visit(MethodDeclaration method) {
			if (method.getName().toString().compareTo(this.methodLookingFor) == 0
					&& method.parameters().toString().equals(methodParameters.toString())) {
				methodDeclaration = method;
				found = true;
				return false;
			} else {
				return true;
			}
		}
	}

	/**
	 * Returns the length, in characters, of the import declaration on the given
	 * compilation unit.
	 * 
	 * @param compilationUnit under processing
	 * @return number of characters of the import declaration
	 */
	public static int lenghtImportDeclaration(CompilationUnit compilationUnit) {
		int result = 0;

		for (Object importDeclaration : compilationUnit.imports()) {
			result = result + ((ImportDeclaration) importDeclaration).getLength();
		}

		return result;
	}
}
