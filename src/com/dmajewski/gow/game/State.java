package com.dmajewski.gow.game;

public class State implements Cloneable {
	private int[][] board = new int[8][8];
	private int[][] mark = new int[8][8];
	private float [][][] boardColors = new float[8][8][3]; 
	
	public int[][] getBoard() {
		return board;
	}

	public void setBoard(int[][] board) {
		this.board = board;
	}

	public int[][] getMark() {
		return mark;
	}

	public void setMark(int[][] mark) {
		this.mark = mark;
	}

	@Override
	protected State clone() throws CloneNotSupportedException {
		State s = new State();
		s.setBoard(deepCopyIntMatrix(board));
		return s;
	}

	public static int[][] deepCopyIntMatrix(int[][] input) {
		if (input == null)
			return null;
		int[][] result = new int[input.length][];
		for (int r = 0; r < input.length; r++) {
			result[r] = input[r].clone();
		}
		return result;
	}

	public int[] getUnknownGemPosition(){
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(getBoard()[i][j] == -1){
					return new int[] {i, j};
				}
			}
		}
		return new int[] {-1, -1};
	}
	
	public String printBoard() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(getBoard()[i][j] == -1){
					throw new RuntimeException("Unknown field: " + i + " " + j + "  = " + -1);
				}
				sb.append(getBoard()[i][j] + " ");
			}
			sb.append("\n");
		}
		//System.out.println(sb);
		return sb.toString();
	}
	
	public void cleanMarks(){
		mark = new int[8][8];
	}

	public float [][][] getBoardColors() {
		return boardColors;
	}

	public void setBoardColors(float [][][] boardColors) {
		this.boardColors = boardColors;
	}

}
