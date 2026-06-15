package main.neo.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import main.neo.app.Constants;
import main.neo.core.jdt.CodeExtractionMetrics;
import main.neo.core.jdt.Utils;

/**
 * A Solution represents a candidate refactoring plan for a method.
 * <p>
 * It consists of a list of {@link Sequence}s, each representing a block of code
 * to be extracted into a separate method. The class manages the evaluation
 * (fitness), feasibility, and application of these refactorings, both as a
 * destructive operation (real workspace edit) and as a non-destructive
 * in-memory simulation.
 * </p>
 */
public class Solution {

	private static final Logger LOGGER = Logger.getLogger(Solution.class.getName());

	/**
	 * Prefix for the JFace position category used to track how extraction range
	 * offsets move while the source is rewritten. A unique suffix is appended per
	 * run so concurrent simulations never share a category on the same document.
	 */
	private static final String OFFSET_TRACKING_CATEGORY = "main.neo.core.Solution.extractionOffsets.";

	/** List of code sequences to extract, ordered by source position. */
	private List<Sequence> sequenceList;

	/** The CompilationUnit this Solution belongs to. */
	private CompilationUnit compilationUnit;

	/** The {@link MethodDeclaration} node of the method being refactored. */
	private ASTNode method;

	/** Name of the method being refactored. */
	private String methodName;

	/** True if all code extractions in the list are valid and applicable. */
	private boolean feasible = false;

	/** Quality of the Solution (lower is better). */
	private double fitness;

	/** Initial cognitive complexity of the method before refactoring. */
	private int initialComplexity;

	/** Total reduction in cognitive complexity if the Solution is applied. */
	private int reducedComplexity;

	/** Aggregated metrics of the code extractions associated with this Solution. */
	private main.neo.core.jdt.CodeExtractionMetricsStats extractionMetricsStats = null;

	/**
	 * Cognitive complexity threshold used to penalise solutions whose extracted
	 * methods (or remaining body) still exceed the limit. Defaults to the global
	 * constant; can be overridden per project via {@link #setThreshold(int)}.
	 */
	private int threshold = Constants.COGNITIVE_COMPLEXITY_THRESHOLD;

	public Solution(List<Sequence> sequenceList, CompilationUnit compilationUnit, ASTNode methodDeclarationNode) {
		this.sequenceList = sequenceList;
		this.compilationUnit = compilationUnit;
		this.method = methodDeclarationNode;
		this.methodName = ((MethodDeclaration) this.method).getName().toString();
		this.initialComplexity = Utils.getIntegerPropertyOfNode(method,
				Constants.ACCUMULATED_CONTRIBUTION_TO_COGNITIVE_COMPLEXITY);
		this.fitness = Double.MAX_VALUE;
	}

	public Solution(CompilationUnit compilationUnit, ASTNode methodDeclarationNode) {
		this(new ArrayList<>(), compilationUnit, methodDeclarationNode);
	}

	public Solution(CompilationUnit compilationUnit, String solutionString, int methodCognitiveComplexity) {
		this.sequenceList = new ArrayList<>();
		this.compilationUnit = compilationUnit;

		Pattern p = Pattern.compile("\\[([0-9\\s]+)\\]");
		Matcher m = p.matcher(solutionString);

		while (m.find()) {
			String value = m.group(1);
			String[] offsets = value.split("\\s+");

			List<ASTNode> nodes = new ArrayList<>();
			for (String offsetStr : offsets) {
				if (offsetStr.isEmpty())
					continue;
				int offset = Integer.parseInt(offsetStr);
				NodeFinder finder = new NodeFinder(compilationUnit, offset, 0);
				ASTNode node = finder.getCoveringNode();
				if (node != null) {
					nodes.add(node);
				}
			}
			if (!nodes.isEmpty()) {
				this.sequenceList.add(new Sequence(compilationUnit, nodes));
			}
		}

		if (!sequenceList.isEmpty()) {
			this.method = sequenceList.get(0).getMethodDeclaration();
			this.methodName = ((MethodDeclaration) this.method).getName().toString();
		}
		this.initialComplexity = methodCognitiveComplexity;
		this.fitness = Double.MAX_VALUE;
	}

	public Solution(Solution other) {
		this.compilationUnit = other.compilationUnit;
		this.method = other.method;
		this.methodName = ((MethodDeclaration) other.method).getName().toString();
		this.sequenceList = new ArrayList<>();
		for (Sequence s : other.sequenceList) {
			this.sequenceList.add(s.copy());
		}
		this.fitness = other.fitness;
		this.feasible = other.feasible;
		this.initialComplexity = other.initialComplexity;
		this.reducedComplexity = other.reducedComplexity;
		this.extractionMetricsStats = other.extractionMetricsStats;
		this.threshold = other.threshold;
	}

	/**
	 * Configures the cognitive complexity threshold used during fitness
	 * evaluation.
	 *
	 * @param threshold project-specific threshold
	 * @return this solution (fluent API)
	 */
	public Solution setThreshold(int threshold) {
		this.threshold = threshold;
		return this;
	}

	public int getThreshold() {
		return threshold;
	}

	public CodeExtractionMetrics evaluate(main.neo.core.refactoringcache.RefactoringCache rf) {
		int complexityOfNewExtractedMethod;
		CodeExtractionMetrics[] metrics = new CodeExtractionMetrics[sequenceList.size()];
		CodeExtractionMetrics results = new CodeExtractionMetrics(true, "", false, 0, 0, new ArrayList<Change>(),
				new ArrayList<Change>(), 0);

		ExtractionTextRange currentRange = null;
		ExtractionTextRange lastRange = null;

		fitness = sequenceList.size();
		reducedComplexity = 0;

		for (int i = sequenceList.size() - 1; i >= 0; i--) {

			if (metrics[i] == null) {
				metrics[i] = sequenceList.get(i).evaluate(rf);
			}

			if (!metrics[i].isFeasible()) {
				fitness = Double.MAX_VALUE;
				feasible = false;
				reducedComplexity = 0;
				return metrics[i];
			}

			currentRange = sequenceList.get(i).getTextRange();

			if (lastRange != null) {
				int innerIndex = i;
				ExtractionTextRange potentialParentRange = currentRange;

				while (innerIndex >= 0) {
					if (lastRange.isContainedIn(potentialParentRange)) {

						if (metrics[innerIndex] == null) {
							metrics[innerIndex] = sequenceList.get(innerIndex).evaluate(rf);
						}

						CodeExtractionMetrics parentMetrics = metrics[innerIndex];
						CodeExtractionMetrics childMetrics = metrics[i + 1];

						parentMetrics.setReductionOfCognitiveComplexity(parentMetrics.getReductionOfCognitiveComplexity()
								- childMetrics.getReductionOfCognitiveComplexity());
						parentMetrics.setAccumulatedInherentComponent(
								parentMetrics.getAccumulatedInherentComponent() - childMetrics.getAccumulatedInherentComponent());
						parentMetrics.setNumberNestingContributors(
								parentMetrics.getNumberNestingContributors() - childMetrics.getNumberNestingContributors());
						parentMetrics.setAccumulatedNestingComponent(childMetrics.getNesting() - parentMetrics.getNesting());
					}

					if (innerIndex > 0) {
						potentialParentRange = sequenceList.get(innerIndex - 1).getTextRange();
					}
					innerIndex--;
				}
			}

			complexityOfNewExtractedMethod = metrics[i].getCognitiveComplexityOfNewExtractedMethod();
			if (complexityOfNewExtractedMethod > threshold) {
				fitness += (complexityOfNewExtractedMethod - threshold) * 10;
			}

			reducedComplexity += metrics[i].getReductionOfCognitiveComplexity();
			results.joinMetrics(metrics[i]);

			lastRange = currentRange;
		}

		this.extractionMetricsStats = new main.neo.core.jdt.CodeExtractionMetricsStats(metrics);
		int finalMethodComplexity = this.initialComplexity - reducedComplexity;

		if (finalMethodComplexity > threshold) {
			fitness += (finalMethodComplexity - threshold) * 10;
		}

		feasible = results.isFeasible();

		return results;
	}

	public boolean isFeasible() {
		return feasible;
	}

	public void removeSequence(int i) {
		this.sequenceList.remove(i);
	}

	public List<Sequence> getSequenceList() {
		return this.sequenceList;
	}

	public Sequence getSequence(int i) {
		return this.sequenceList.get(i);
	}

	public int getSize() {
		return this.sequenceList.size();
	}

	public String getMethodName() {
		return this.methodName;
	}

	public MethodDeclaration getMethodDeclaration() {
		return (MethodDeclaration) method;
	}

	public boolean contains(ASTNode node) {
		return sequenceList.stream().anyMatch(s -> s.contains(node));
	}

	@Override
	public String toString() {
		return "Solution [methodName=" + methodName + ", sequenceList=" + sequenceList + ", isFeasible=" + feasible
				+ ", fitness=" + fitness + ", reducedComplexity=" + reducedComplexity + "]";
	}

	public String toStringForFileFormat() {
		return this.sequenceList.toString();
	}

	public double getFitness() {
		return fitness;
	}

	public int getReducedComplexity() {
		return reducedComplexity;
	}

	public int getInitialComplexity() {
		return initialComplexity;
	}

	public main.neo.core.jdt.CodeExtractionMetricsStats getExtractionMetricsStats() {
		return extractionMetricsStats;
	}

	/** @return text ranges for the sequences in this solution. */
	public List<ExtractionTextRange> getRanges() {
		return sequenceList.stream().map(Sequence::getTextRange).collect(Collectors.toList());
	}

	// =========================================================================
	// Refactoring Application Logic
	// =========================================================================

	/**
	 * Result of a non-destructive simulation of the extractions: the resulting
	 * source code and its parsed AST.
	 */
	public static final class SimulationResult {
		private final CompilationUnit compilationUnit;
		private final String source;

		public SimulationResult(CompilationUnit compilationUnit, String source) {
			this.compilationUnit = compilationUnit;
			this.source = source;
		}

		public CompilationUnit getCompilationUnit() {
			return compilationUnit;
		}

		public String getSource() {
			return source;
		}
	}

	/**
	 * Applies the extractions destructively on the underlying compilation unit.
	 * <p>
	 * Use this only after the user has confirmed the refactoring; for previewing,
	 * use {@link #simulateExtractMethods(ICompilationUnit)}.
	 * </p>
	 */
	public boolean applyExtractMethodsRefactoring() throws CoreException {
		try {
			return applyExtractionsInternal(false, null);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Unexpected I/O error while applying extractions", e);
			return false;
		}
	}

	/**
	 * Simulates the extractions in memory without modifying the workspace file.
	 * <p>
	 * Each extraction is computed using JDT in-memory refactoring (the Change is
	 * never performed on disk); the resulting {@link TextEdit} is applied to a
	 * {@link Document} that holds the running source code, and a fresh AST is
	 * parsed before the next extraction so offsets and bindings stay coherent.
	 * </p>
	 * <p>
	 * <b>Non-destructive guarantee:</b> the file backing {@code baseIcu} is never
	 * written. Only the in-memory buffer of the supplied working copy is updated
	 * (so bindings resolve for the next step); the caller is responsible for
	 * discarding that working copy, which drops every simulated change. The actual
	 * substitution of the file happens later, on explicit user confirmation,
	 * through a separate apply step ({@code RefactorApplier}). For this reason
	 * {@code baseIcu} <b>must be a working copy</b> ({@code isWorkingCopy() == true}).
	 * </p>
	 *
	 * @param baseIcu in-memory working copy used as the parsing context for
	 *                resolving bindings during simulation; its file is not modified
	 * @return the simulated source and AST after applying every extraction, or
	 *         {@code null} if any extraction is not feasible in simulation mode
	 */
	public SimulationResult simulateExtractMethods(ICompilationUnit baseIcu) throws CoreException {
		try {
			SimulationContext ctx = new SimulationContext(baseIcu);
			boolean ok = applyExtractionsInternal(true, ctx);
			if (!ok) {
				return null;
			}
			return new SimulationResult(this.compilationUnit, ctx.document.get());
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Unexpected I/O error while simulating extractions", e);
			return null;
		}
	}

	/**
	 * Holds the in-memory document and parsing context used while simulating a
	 * sequence of extractions.
	 */
	private static final class SimulationContext {
		final ICompilationUnit baseIcu;
		final IDocument document;

		SimulationContext(ICompilationUnit baseIcu) throws CoreException {
			this.baseIcu = baseIcu;
			this.document = new Document(baseIcu.getSource());
		}
	}

	/**
	 * Internal engine that performs (or simulates) the extractions.
	 * <p>
	 * When {@code simulate} is {@code true}, each extraction is computed with
	 * {@link Utils#extractCode(CompilationUnit, int, int, String, boolean)} in
	 * simulation mode; the resulting {@link TextEdit} is applied to
	 * {@code simCtx.document} and the AST is rebuilt from that document before
	 * processing the next extraction. The workspace file is never modified.
	 * </p>
	 */
	private boolean applyExtractionsInternal(boolean simulate, SimulationContext simCtx)
			throws CoreException, IOException {
		boolean extractionsApplied = true;
		List<ExtractionTextRange> rangesForExtractions = getRanges();
		Stack<CodeExtractionMetrics> extractionMetricsStack = new Stack<>();

		// As every extraction rewrites the source, the character offsets of the
		// ranges still to be processed shift: a containing ("parent") range
		// shrinks when an inner ("child") range is replaced by a method call, and
		// every range moves to the right when an import is added. The previous
		// implementation recomputed those offsets with hand-written arithmetic,
		// which was brittle and could produce a selection that no longer matched a
		// statement boundary (raising "the selection is not valid" while
		// extracting). Instead, we register each range as a JFace Position in the
		// running document; the DefaultPositionUpdater then moves every position
		// exactly as the applied TextEdit changes the underlying text.
		IDocument trackingDocument = obtainTrackingDocument(simulate, simCtx);
		String positionCategory = OFFSET_TRACKING_CATEGORY + System.nanoTime();
		List<Position> trackedPositions = registerTrackingPositions(trackingDocument, positionCategory,
				rangesForExtractions);

		try {
			// loop for code extractions: from back to front so that an already
			// processed (later) extraction never invalidates the offsets of a range
			// still to be processed (which always sits earlier in the source).
			int indexOfExtraction = rangesForExtractions.size() - 1;

			while (indexOfExtraction >= 0 && extractionsApplied) {
				Position current = trackedPositions.get(indexOfExtraction);

				// Skip ranges that an earlier extraction has already absorbed.
				if (current.isDeleted() || current.getLength() == 0) {
					indexOfExtraction--;
					continue;
				}

				int selectionStart = current.getOffset();
				int selectionLength = current.getLength();

				// Generate the name of the method that will hold the extracted code.
				String currentMethodName = this.methodName + "_ext_" + (indexOfExtraction + 1);
				currentMethodName = currentMethodName.replaceFirst("^.",
						currentMethodName.substring(0, 1).toLowerCase());

				// Perform (or simulate) the extraction against the current source state.
				CodeExtractionMetrics extractionMetrics = Utils.extractCode(compilationUnit, selectionStart,
						selectionLength, currentMethodName, simulate);
				extractionsApplied = extractionMetrics.isFeasible() && (simulate || extractionMetrics.isApplied());

				if (!extractionsApplied) {
					LOGGER.warning("ERROR extracting sequence " + sequenceList.get(indexOfExtraction) + ": "
							+ extractionMetrics.getReason());
					if (!simulate) {
						while (!extractionMetricsStack.isEmpty())
							extractionMetricsStack.pop().getUndoChanges().get(0).perform(null);
					}
					break;
				}

				extractionMetricsStack.push(extractionMetrics);

				// Apply the change to the running source and rebuild the AST. The same
				// TextEdit is applied to the tracking document, which precisely shifts
				// the Positions of the ranges that remain to be processed.
				if (simulate) {
					compilationUnit = applySimulatedChange(extractionMetrics, simCtx);
				} else {
					applyEditToTrackingDocument(extractionMetrics, trackingDocument);
					compilationUnit = Utils.createCompilationUnitFromFileInWorkspace(
							compilationUnit.getJavaElement().getPath().toOSString());
				}

				indexOfExtraction--;
			}
		} finally {
			removeTrackingCategory(trackingDocument, positionCategory);
		}

		return extractionsApplied;
	}

	/**
	 * Returns the document used to track how range offsets move as the extractions
	 * rewrite the source.
	 * <p>
	 * In simulation mode this is the very document that holds the running source
	 * ({@link SimulationContext#document}); applying each simulated {@link TextEdit}
	 * to it therefore also moves the tracked positions. In the destructive mode a
	 * parallel document mirroring the source is created so the same exact offset
	 * tracking applies even though the AST is reparsed from the workspace file.
	 * </p>
	 */
	private IDocument obtainTrackingDocument(boolean simulate, SimulationContext simCtx) throws CoreException {
		if (simulate) {
			return simCtx.document;
		}
		ICompilationUnit icu = Utils.getICompilationUnit(compilationUnit);
		String source = (icu != null) ? icu.getSource() : "";
		return new Document(source);
	}

	/**
	 * Registers every extraction range as a {@link Position} in the tracking
	 * document under a dedicated category handled by a {@link DefaultPositionUpdater}.
	 *
	 * @return the list of positions, aligned by index with {@code ranges}.
	 */
	private static List<Position> registerTrackingPositions(IDocument document, String category,
			List<ExtractionTextRange> ranges) throws IOException {
		document.addPositionCategory(category);
		document.addPositionUpdater(new DefaultPositionUpdater(category));
		List<Position> positions = new ArrayList<>(ranges.size());
		try {
			for (ExtractionTextRange range : ranges) {
				int length = Math.max(0, range.getEnd() - range.getStart());
				Position position = new Position(range.getStart(), length);
				document.addPosition(category, position);
				positions.add(position);
			}
		} catch (BadLocationException | BadPositionCategoryException e) {
			throw new IOException("Failed to register offset-tracking positions for extraction", e);
		}
		return positions;
	}

	/**
	 * Removes the offset-tracking category from the document. The associated
	 * {@link DefaultPositionUpdater} is intentionally left registered: it silently
	 * ignores changes once its category no longer exists, and the document is not
	 * modified after this point.
	 */
	private static void removeTrackingCategory(IDocument document, String category) {
		try {
			document.removePositionCategory(category);
		} catch (BadPositionCategoryException ignore) {
			// The category was never fully registered; nothing else to clean up.
		}
	}

	/**
	 * Applies the extraction's {@link TextEdit} to the offset-tracking document so
	 * the positions of the not-yet-processed ranges move with the text. Used by the
	 * destructive path, where the AST itself is reparsed from the workspace file.
	 */
	private static void applyEditToTrackingDocument(CodeExtractionMetrics metrics, IDocument document)
			throws IOException {
		if (metrics.getChanges().isEmpty()) {
			return;
		}
		TextEdit edit = Utils.extractTextEdit(metrics.getChanges().get(0));
		if (edit == null) {
			return;
		}
		try {
			// Apply a copy: applying a TextEdit mutates its internal state and the
			// original Change may still be needed by the caller.
			edit.copy().apply(document);
		} catch (MalformedTreeException | BadLocationException e) {
			throw new IOException("Failed to update offset-tracking document: " + e.getMessage(), e);
		}
	}

	/**
	 * Applies a simulated extraction to the running document and reparses the AST
	 * with the project context to keep bindings available for the next iteration.
	 * <p>
	 * After applying the TextEdit to the in-memory document, the working copy's
	 * buffer is updated so the reparsed AST retains a valid {@code typeRoot}
	 * reference. This guarantees that subsequent calls to
	 * {@link Utils#getICompilationUnit(CompilationUnit)} will return the working
	 * copy and {@link Utils#extractCode} can operate on it.
	 * </p>
	 */
	private static CompilationUnit applySimulatedChange(CodeExtractionMetrics metrics, SimulationContext simCtx)
			throws IOException {
		if (metrics.getChanges().isEmpty()) {
			throw new IOException("Simulation produced no Change to apply");
		}
		Change change = metrics.getChanges().get(0);
		TextEdit edit = Utils.extractTextEdit(change);
		if (edit == null) {
			throw new IOException("Simulation Change has no TextEdit to apply");
		}
		try {
			// Defensive copy: applying a TextEdit mutates its internal state
			// (e.g. MoveSourceEdit's source computation), making it non-reusable.
			edit.copy().apply(simCtx.document);
		} catch (MalformedTreeException | BadLocationException e) {
			throw new IOException("Failed to apply simulated TextEdit: " + e.getMessage(), e);
		}

		// Safety guard: a simulation must NEVER alter the file on disk. We only ever
		// mutate the *in-memory* buffer of a working copy (which the caller discards
		// without committing), never a primary compilation unit. Refusing the latter
		// keeps the "planning never touches the original file" guarantee even if a
		// future caller mistakenly passes a non-working-copy.
		if (!simCtx.baseIcu.isWorkingCopy()) {
			throw new IOException("Refusing to mutate a primary compilation unit during simulation; "
					+ "an in-memory working copy is required so the original file stays untouched");
		}

		try {
			// Sync the working copy buffer with the updated document content so the
			// resulting CompilationUnit has a valid typeRoot (Java model element).
			// This change lives only in memory and is dropped on discardWorkingCopy().
			simCtx.baseIcu.getBuffer().setContents(simCtx.document.get().toCharArray());

			// Reparse from the ICompilationUnit to preserve typeRoot linkage.
			ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(simCtx.baseIcu);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			return (CompilationUnit) parser.createAST(new NullProgressMonitor());
		} catch (Exception e) {
			throw new IOException("Failed to update working copy buffer: " + e.getMessage(), e);
		}
	}
}
