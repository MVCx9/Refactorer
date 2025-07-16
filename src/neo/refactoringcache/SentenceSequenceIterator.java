package neo.refactoringcache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EmptyStatement;

import neo.Constants;
import neo.Utils;
import neo.algorithms.Sequence;
import neo.cem.CodeExtractionMetrics;
import neo.refactoringcache.ConsecutiveSequenceIterator.SentenceSequenceInfo;

public class SentenceSequenceIterator implements Iterable<List<Sequence>> {
	// TODO: efficiency

	private CompilationUnit compilationUnit;
	private Sequence sentences;
	private ConsecutiveSequenceIterator csi;
	private Iterable<List<Sequence>> iterable = null;

	public SentenceSequenceIterator(Sequence sentences, RefactoringCache refactoringCache,
			ConsecutiveSequenceIterator.APPROACH approach) {
		this.compilationUnit = refactoringCache.getCompilationUnit();
		this.sentences = sentences;
		csi = new ConsecutiveSequenceIterator(new SentenceSequenceInfo() {
			@Override
			public int numberOfSentences() {
				return sentences.getSiblingNodes().size();
			}

			@Override
			public int cognitiveComplexityOfSentence(int sentence) {
				Integer acc = (Integer) sentences.getSiblingNodes().get(sentence - 1)
						.getProperty(Constants.ACCUMULATED_COMPLEXITY);
				if (acc == null) {
					acc = 0;
				}
				return acc;
			}

			@Override
			public boolean validSequence(int from, int to) {
				if (isEmptyStatement(from) || isEmptyStatement(to)) {
					return false;
				}
				CodeExtractionMetrics cem = refactoringCache
						.getMetrics(new Sequence(refactoringCache.getCompilationUnit(), sentences.getSiblingNodes().subList(from - 1, to)));
				return cem.isFeasible();
			}

			private boolean isEmptyStatement(int sentence) {
				ASTNode node = sentences.getSiblingNodes().get(sentence - 1);
				return (node instanceof EmptyStatement);
			}
		}, approach);
	}

	public Iterable<List<Sequence>> getIterable() {
		if (iterable == null) {
			iterable = Utils.adapt(csi.getIterable(), this::adapt);
		}
		return iterable;
	}

	private List<Sequence> adapt(Stack<Integer> stack) {
		List<Sequence> result = new ArrayList<>();
		for (int i = 0; i < stack.size(); i = i + 2) {
			ArrayList<ASTNode> nodes = new ArrayList<>(
					sentences.getSiblingNodes().subList(stack.get(i) - 1, stack.get(i + 1)));
			result.add(new Sequence(this.compilationUnit, nodes));
		}
		return result;
	}

	@Override
	public Iterator<List<Sequence>> iterator() {
		return getIterable().iterator();
	}

}
