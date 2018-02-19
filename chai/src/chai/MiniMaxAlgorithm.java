/*
 * Author: Mahesh Devalla
 * Artificial Intelligence
 * Assignment: Chess AI
 * Credits: Stubs provided by Professor Devin Balkcom
 * References:
 * https://en.wikipedia.org/wiki/Solving_chess
 * https://erikbern.com/2014/11/29/deep-learning-for-chess/
 * https://chessprogramming.wikispaces.com/
 * http://www-03.ibm.com/ibm/history/ibm100/us/en/icons/deepblue/
 */
package chai;

import chesspresso.move.*;
import chesspresso.position.*;

public class MiniMaxAlgorithm implements ChessAI {
	private int noOfNodes = 0;
	private int currdepth = 0;
	private static int depth = 2;
	private static int game;
//inner class needed to traverse in the tree 
	private class Node {

		private int node, hueristic;

		public Node(int node, int hueristic) {
			this.node = node;
			this.hueristic = hueristic;
		}

		public int getHueristic() {
			return hueristic;
		}

		public short getNode() {
			return (short) this.node;
		}
	}

	@Override
	public short getMove(Position pos) throws Exception {

		game = pos.getToPlay();
		Node nextNode = null;
		int tempdepth = depth;
		for (int i = 1; i <= tempdepth; i++) {
			depth = i;
			nextNode = miniMaxAlgorithm(pos);
			resetStats();
			currdepth = 0;

			if (nextNode.getHueristic() == Integer.MAX_VALUE) {
				return nextNode.getNode();
			}
		}

		return nextNode.getNode();
	}

	private void resetStats() {
		// TODO Auto-generated method stub
		setNoOfNodes(0);

	}

	private Node miniMaxAlgorithm(Position pos) {

		short[] nextNodes = pos.getAllMoves();
		Node nextNodeSuccesor = null;
		// pseudo code from
		// https://en.wikipedia.org/wiki/Rules_of_chess#Illegal_position
		try {
			pos.doMove(nextNodes[0]);
			nextNodeSuccesor = new Node(nextNodes[0], calcMin(pos, 1));
			pos.undoMove();
		} catch (IllegalMoveException e) {
			// System.out.println("This is not a valid move");
			e.printStackTrace();

		}
		for (int i = 1; i < nextNodes.length; i++) {
			try {
				pos.doMove(nextNodes[i]);
				Node temp = new Node(nextNodes[i], calcMin(pos, 1));
				pos.undoMove();
				if (temp.getHueristic() >= nextNodeSuccesor.getHueristic()) {
					nextNodeSuccesor = temp;
				}
			} catch (IllegalMoveException e) {
				// System.out.println("This is not a valid move");
				e.printStackTrace();
			}
		}
		return nextNodeSuccesor;
	}

	private int calcMin(Position pos, int tempdepth) {

		setNoOfNodes(getNoOfNodes() + 1);
		currdepth = Math.max(tempdepth, currdepth);
		if (pos.isTerminal() || tempdepth == depth) {
			// Pseudo code from
			// https://www.cs.cornell.edu/courses/cs312/2002sp/lectures/rec21.htm
			if (pos.isTerminal()) {

				if (pos.isStaleMate()) {
					return 0;
				}

				else if (pos.isMate()) {
					return (pos.getToPlay() != game) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
				} else {
					return 0;
				}
			}

			else {
				return (pos.getToPlay() == game) ? pos.getMaterial() : (-1 * pos.getMaterial());
			}
		}
		int minOfMaxVal = Integer.MAX_VALUE;
		for (short temp : pos.getAllMoves()) {
			try {
				pos.doMove(temp);
				minOfMaxVal = Math.min(minOfMaxVal, calcMax(pos, tempdepth + 1));
				pos.undoMove();
			}

			catch (IllegalMoveException e) {
				// System.out.println("This is not a valid move");
				e.printStackTrace();
			}
		}

		return minOfMaxVal;
	}

	private int calcMax(Position pos, int tempdepth) {
		setNoOfNodes(getNoOfNodes() + 1);
		currdepth = Math.max(tempdepth, currdepth);
		// Pseudo code from
		// https://www.cs.cornell.edu/courses/cs312/2002sp/lectures/rec21.htm
		if (pos.isTerminal() || tempdepth == depth) {
			if (pos.isTerminal()) {
				if (pos.isStaleMate()) {
					return 0;
				}

				else if (pos.isMate()) {

					return (pos.getToPlay() != game) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
				}

				else {
					return 0;
				}
			}

			else {
				return (pos.getToPlay() == game) ? pos.getMaterial() : (-1 * pos.getMaterial());
			}
		}

		int maxOfMinVal = Integer.MIN_VALUE;
		for (short temp : pos.getAllMoves()) {
			try {
				pos.doMove(temp);
				maxOfMinVal = Math.max(maxOfMinVal, calcMin(pos, tempdepth + 1));
				pos.undoMove();
			}

			catch (IllegalMoveException e) {
				// System.out.println("This is not a valid move");
				e.printStackTrace();
			}
		}

		return maxOfMinVal;
	}

	public int getNoOfNodes() {
		return noOfNodes;
	}

	public void setNoOfNodes(int noOfNodes) {
		this.noOfNodes = noOfNodes;
	}

}