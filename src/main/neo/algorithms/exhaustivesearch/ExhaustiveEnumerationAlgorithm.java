package main.neo.algorithms.exhaustivesearch;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;

import main.neo.algorithms.Sequence;
import main.neo.refactoringcache.ConsecutiveSequenceIterator.APPROACH;
import main.neo.refactoringcache.RefactoringCache;
import main.neo.refactoringcache.SentenceSequenceIterator;
import main.neo.refactoringcache.SentencesSelectorVisitor;

public class ExhaustiveEnumerationAlgorithm {
	private ASTNode method;
	private RefactoringCache refactoringCache;
	private SentencesSelectorVisitor sentencesSelectorVisitor;
	private APPROACH approach;

	public ExhaustiveEnumerationAlgorithm(RefactoringCache refactoringCache, ASTNode method, APPROACH approach) {
		this.refactoringCache = refactoringCache;
		this.method = method;
		this.approach = approach;
		sentencesSelectorVisitor = new SentencesSelectorVisitor(refactoringCache.getCompilationUnit());
		method.accept(sentencesSelectorVisitor);
	}

	public void run(Consumer<List<Sequence>> consumer, long maxElements) {
		List<Iterable<List<Sequence>>> elementsToIterate = sentencesSelectorVisitor.getSentencesToIterate().stream()
				.map(sequence -> new SentenceSequenceIterator(sequence, refactoringCache, approach))
				.collect(Collectors.toList());

		ExhaustiveEnumeration<List<Sequence>> ee = new ExhaustiveEnumeration<>(elementsToIterate, t -> true);

		ee.run(solution -> {
			List<Sequence> result = solution.stream().reduce(new ArrayList<>(), (a, b) -> {
				a.addAll(b);
				return a;
			});
			consumer.accept(result);
		}, maxElements);

	}

	public BigInteger count() {
		List<Iterable<List<Sequence>>> elementsToIterate = sentencesSelectorVisitor.getSentencesToIterate().stream()
				.map(sequence -> new SentenceSequenceIterator(sequence, refactoringCache, approach))
				.collect(Collectors.toList());

		ExhaustiveEnumeration<List<Sequence>> ee = new ExhaustiveEnumeration<>(elementsToIterate, t -> true);
		return ee.count();
	}
}
