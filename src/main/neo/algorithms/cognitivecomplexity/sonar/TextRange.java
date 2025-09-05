package main.neo.algorithms.cognitivecomplexity.sonar;

public class TextRange {

	private Integer startLine;
	private Integer endLine;
	private Integer startOffset;
	private Integer endOffset;

	public Integer getStartLine() {
		return startLine;
	}

	public void setStartLine(Integer startLine) {
		this.startLine = startLine;
	}

	public Integer getEndLine() {
		return endLine;
	}

	public void setEndLine(Integer endLine) {
		this.endLine = endLine;
	}

	public Integer getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(Integer startOffset) {
		this.startOffset = startOffset;
	}

	public Integer getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(Integer endOffset) {
		this.endOffset = endOffset;
	}

	@Override
	public String toString() {
		return new String("[" + startLine + "," + endLine + "] offset=[" + startOffset + "," + endOffset + "]");
	}
}
