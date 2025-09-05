package main.neo.algorithms.cognitivecomplexity.sonar;

import java.util.List;

public class ProjectAnalysis {

	private Paging paging;
	private List<Analysis> analyses = null;

	public Paging getPaging() {
		return paging;
	}

	public void setPaging(Paging paging) {
		this.paging = paging;
	}

	public List<Analysis> getAnalyses() {
		return analyses;
	}

	public void setAnalyses(List<Analysis> analyses) {
		this.analyses = analyses;
	}
}
