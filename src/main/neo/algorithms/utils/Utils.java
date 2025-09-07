package main.neo.algorithms.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import main.neo.algorithms.Solution;

public class Utils {

	/**
	 * Get the index in a list where a solutions must be inserted to keep the list
	 * sorted Note that solutions are ordered taking into account the offset in
	 * characters of extractions in the compilation unit
	 * 
	 * @param solution to insert in the list
	 * @param list     where insert the solution
	 * @return Position in the list where insert solution to keep the list sorted
	 */
	public static int indexOfInsertionToKeepListSorted(Solution solution, List<Solution> list) {
		int positionOfInsertion = 0;
		boolean nodeInserted = false;
		int startPositionOfNodeToInsert = solution.getSequenceList().get(0).getSiblingNodes().get(0).getStartPosition();

		while (!list.isEmpty() && !nodeInserted && positionOfInsertion < list.size()) {
			int startPositionOfCurrentNode = list.get(positionOfInsertion).getSequenceList().get(0).getSiblingNodes()
					.get(0).getStartPosition();
			if (startPositionOfNodeToInsert < startPositionOfCurrentNode) {
				nodeInserted = true;
			} else
				positionOfInsertion++;
		}

		return positionOfInsertion;
	}

	/**
	 * Get the index in a list where a node must be inserted to keep the list sorted
	 * Note that nodes are ordered taking into account their offset in characters in
	 * the compilation unit
	 * 
	 * @param node to insert in the list
	 * @param list where insert the node
	 * @return Position in the list where insert node to keep the list sorted
	 */
	public static int indexOfInsertionToKeepListSorted(ASTNode node, List<ASTNode> list) {
		int positionOfInsertion = 0;
		boolean nodeInserted = false;
		int startPositionOfNodeToInsert = node.getStartPosition();

		while (!list.isEmpty() && !nodeInserted && positionOfInsertion < list.size()) {
			int startPositionOfCurrentNode = list.get(positionOfInsertion).getStartPosition();
			if (startPositionOfNodeToInsert < startPositionOfCurrentNode) {
				nodeInserted = true;
			} else
				positionOfInsertion++;
		}

		return positionOfInsertion;
	}

	/**
	 * Compose the URI to query in order to get cognitive complexity issues for the
	 * given project, sonar server and page index (results reported by sonar are
	 * usually paginated)
	 * 
	 * @param server
	 * @param project
	 * @param page
	 * @return The URL to query to get cognitive complexity issues of the given
	 *         project
	 */
	public static String composeSonarUri(String server, String project, int page) {
		String result = new String(server + "/api/issues/search?rules=squid:S3776" + "&componentKeys=" + project
				+ "&resolved=false" + "&p=" + page + "&pageSize=100");

		return result;
	}

}
