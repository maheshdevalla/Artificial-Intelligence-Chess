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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;
import chesspresso.game.Game;
import chesspresso.move.*;
import chesspresso.pgn.PGNReader;
import chesspresso.position.*;

public class AlphaBetaAlgorithm implements ChessAI {
	private Game[] book;
	private boolean runBook = false;
	private boolean TranspositionTable = false;
	private int metric = 1;
	private final static int depth = 5;
	@SuppressWarnings("unused")
	private int alpha, beta;
	HashMap<Integer, TranspositionTable> tt = new HashMap<>();
	//This inner class is used for traversal of the tree
	class Node {
		public int node, hueristic;
		Node() {
			this.node = Integer.MIN_VALUE;
			this.hueristic = Integer.MIN_VALUE;
		}

		Node(int node, int hueristic) {
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

	// This class is used to combine the set of values into transposition function
	// Idea is referenced from
	// http://www.gamedev.net/page/resources/_/technical/artificial-intelligence/chess-programming-part-ii-data-structures-r1046
	class TranspositionTable {
		public int hueristic, versatility, depth;

		TranspositionTable(int hueristic, int versatility, int depth) {
			this.hueristic = hueristic;
			this.versatility = versatility;
			this.depth = depth;
		}

		public int getHueristic() {
			return hueristic;
		}

		public void setHueristic(int hueristic) {
			this.hueristic = hueristic;
		}

		public int getVersatility() {
			return versatility;
		}

		public void setVersatility(int versatility) {
			this.versatility = versatility;
		}

		public int getDepth() {
			return depth;
		}

		public void setDepth(int depth) {
			this.depth = depth;
		}

	}
	private int nextNode(Position pos) {
		return (pos.getToPlay() == alpha) ? pos.getMaterial() : -pos.getMaterial();
	}
	//Pseudo calculations taken from https://chessprogramming.wikispaces.com/ 
	private int calcHueristic(Position pos) {
		if (pos.isTerminal()) {
			if (pos.isStaleMate()) {
				return 0;
			} else if (pos.isMate()) {
				return (pos.getToPlay() == alpha) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
			} else {
				return 0;
			}
		} else {
			return nextNode(pos);
		}

	}

	public void resetStats() {
		beta = 0;
	}

	public void printStats() {
		// System.out.println("Visited Nodes count: "+beta);
	}
	//Pseudo calculations taken from https://chessprogramming.wikispaces.com/
	private int calcMax(Position pos, int depth, int alpha, int beta) {
		noOfNodes();
		int tempdepth = Integer.MIN_VALUE;
		if (TranspositionTable && tt.containsKey(pos.getHashCode()) && tt.get(pos.getHashCode()).depth > metric) 
		{
			int versatility = tt.get(pos.getHashCode()).hueristic;
			if (tt.get(pos.getHashCode()).versatility == 0) 
			{
				return versatility;
			} else if (tt.get(pos.getHashCode()).versatility < 0 && versatility >= beta) 
			{
				return versatility;
			} else if (beta <= alpha) 
			{
				return versatility;
			}
		}
		if (pos.isTerminal() || depth >= metric) 
		{
			int versatility = calcHueristic(pos);

			if (TranspositionTable && versatility <= alpha)
			{
				tt.put((int) pos.getHashCode(), new TranspositionTable(versatility, depth, 1));
			} 
			else if (TranspositionTable && versatility >= beta) 
			{
				tt.put((int) pos.getHashCode(), new TranspositionTable(versatility, depth, -1));
			}
			else if (TranspositionTable) 
			{
				tt.put((int) pos.getHashCode(), new TranspositionTable(versatility, depth, 0));
			}
			return versatility;
		}
		for (short temp : pos.getAllMoves()) 
		{
			try {
				pos.doMove(temp);
				tempdepth = Math.max(tempdepth, calcMin(pos, depth + 1, alpha, beta));
				pos.undoMove();
			} catch (IllegalMoveException e) {
				e.printStackTrace();
			}
			if (tempdepth >= beta) {
				return tempdepth;
			}
			alpha = Math.max(alpha, tempdepth);
		}
		return tempdepth;
	}

	private int calcMin(Position pos, int depth, int alpha, int beta) 
	{
		int min = Integer.MAX_VALUE;
		noOfNodes();
		if (TranspositionTable && tt.containsKey(pos.getHashCode()) && tt.get(pos.getHashCode()).depth > metric)
		{
			int hueristic = tt.get(pos.getHashCode()).hueristic;
			if (tt.get(pos.getHashCode()).versatility == 0)
			{
				return hueristic;
			} else if (tt.get(pos.getHashCode()).versatility < 0 && hueristic >= beta)
			{
				return hueristic;
			} else if (tt.get(pos.getHashCode()).versatility > 0 && hueristic < alpha)
			{
				return hueristic;
			} else if (beta <= alpha) {
				return hueristic;
			}
		}
		if (pos.isTerminal() || depth >= metric) 
		{
			int score = calcHueristic(pos);

			if (TranspositionTable && score <= alpha) 
			{
				tt.put((int) pos.getHashCode(), new TranspositionTable(score, depth, 1));
			} 
			else if (TranspositionTable && score >= beta)
			{
				tt.put((int) pos.getHashCode(), new TranspositionTable(score, depth, -1));
			}
			else if (TranspositionTable)
			{
				tt.put((int) pos.getHashCode(), new TranspositionTable(score, depth, 0));
			}
			return score;
		}
		for (short temp : pos.getAllMoves()) 
		{
			try {
				pos.doMove(temp);
				min = Math.min(min, calcMax(pos, depth + 1, alpha, beta));
				pos.undoMove();
			} catch (IllegalMoveException e) 
			{
				e.printStackTrace();
			}
			if (min <= alpha) {
				return min;
			}
			beta = Math.min(beta, min);
		}
		return min;
	}

	private void noOfNodes() {
		beta++;
	}

	private Node alphaBetaAlgorithm(Position pos) throws IllegalMoveException {
		short nextNode = Short.MIN_VALUE;
		int nextNodeSuccesor = Integer.MIN_VALUE;
		for (short temp : pos.getAllMoves()) {
			pos.doMove(temp);
			int minOfAB = calcMin(pos, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
			if (minOfAB > nextNodeSuccesor) {
				nextNodeSuccesor = minOfAB;
				nextNode = temp;
			}
			pos.undoMove();
		}
		return new Node(nextNode, nextNodeSuccesor);
	}

	@Override
	public short getMove(Position position) throws Exception {
		alpha = position.getToPlay();
		if (runBook) {
			System.out.println("using the book.pgn for the initial moves in the algorithm as an extension");
			// code reference from http://www.cs.dartmouth.edu/~devin/cs76/04_chess/chess.html
			runBook=false;
			book = new Game[120];
			URL url = this.getClass().getResource("book.pgn");
			File f = new File(url.toURI());
			FileInputStream fis = new FileInputStream(f);
			PGNReader pgn = new PGNReader(fis, "book.pgn");
			for (int i = 0; i < 120; i++) {
				Game game = pgn.parseGame();
				game.gotoStart();
				book[i] = game;
			}
		return book(alpha);
		}
		Node nextNode = new Node();
		for (int i = 0; i < depth; i++) {
			metric = i;
			try {
				nextNode = alphaBetaAlgorithm(position);
			} catch (IllegalMoveException e) {
				e.printStackTrace();
			}
			printStats();
			resetStats();

			if (nextNode.hueristic == Integer.MAX_VALUE) {
				// System.out.println("Heuristic function:"+nextNode.hueristic);
				return (short) nextNode.node;
			}
		}
		// System.out.println("The : "+nextNode.hueristic);
		return (short) nextNode.node;
	}
	private short book(int alpha) {
		int rand = new Random().nextInt(120);
		if (alpha == 1)
			book[rand].goForward();
		return book[rand].getNextShortMove();
	}

}
