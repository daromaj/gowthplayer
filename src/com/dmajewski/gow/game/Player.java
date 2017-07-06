package com.dmajewski.gow.game;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

import javax.imageio.ImageIO;

import com.dmajewski.gow.img.ScreenProcessor;
import com.dmajewski.gow.mobile.driver.AdbExec;

public class Player {

	public static void main(String[] args) throws Exception {
		Color avgNumber = new Color(0, 0, 0);
		int exceptionCount = 0;
		int noMove = 0;
		do {
			String fileName = new Date().getTime() + "_temp.png";
			AdbExec.takeScreenshot(fileName);
			State initialState = new State();
//			 String fileName = "1495676158938_temp.png";
			File f = new File(fileName);
			BufferedImage bufferedImage = ImageIO.read(f);
			Color numColor = ScreenProcessor.averageColor(bufferedImage, 1040, 15, 70, 35);
//			avgNumber = numColor;
			if (!numColor.equals(avgNumber)) {
				ScreenProcessor.pixelateScreen(bufferedImage, initialState);
				try {
					initialState.printBoard();
					Move m = getBestMove(initialState);
					System.out.println("Best move: " + m.x + ", " + m.y + " " + (m.direction == 0 ? "right" : "down"));
					AdbExec.swipe(m.x, m.y, m.direction);
					f.delete();
					exceptionCount = 0;		
					avgNumber = numColor;						
				} catch (Exception e) {
					e.printStackTrace();
					exceptionCount++;
				}
			}else{
				noMove++;
			}
			if(noMove > 3){
				avgNumber = new Color(0,0,0);
				noMove = 0;
			}
		} while (true && exceptionCount < 2);

	}

	public static Move getBestMove(State s) throws CloneNotSupportedException {
		Move m = new Move();
		int bestScore = 0;
		for (int i = 0; i < 8; i++) {// row
			for (int j = 0; j < 8; j++) {// column
				for (int dir = 0; dir < 2; dir++) {
					if (dir == 0) {// right
						if (j + 1 > 7 || s.getBoard()[i][j] == s.getBoard()[i][j + 1]) {
							continue;
						}
						State tmp = s.clone();
						int tmpType = tmp.getBoard()[i][j];
						tmp.getBoard()[i][j] = tmp.getBoard()[i][j + 1];
						tmp.getBoard()[i][j + 1] = tmpType;
						// tmp.printBoard();
						int score = getLocalScore(tmp, i, j, tmp.getBoard()[i][j]);
						score += getLocalScore(tmp, i, j + 1, tmp.getBoard()[i][j + 1]);
						if (bestScore < score) {
							m.score = score;
							m.direction = dir;
							m.x = i;
							m.y = j;
							bestScore = score;
							// tmp.printBoard();
						}
					} else {// down
						if (i + 1 > 7 || s.getBoard()[i][j] == s.getBoard()[i + 1][j]) {
							continue;
						}
						State tmp = s.clone();
						int tmpType = tmp.getBoard()[i][j];
						tmp.getBoard()[i][j] = tmp.getBoard()[i + 1][j];
						tmp.getBoard()[i + 1][j] = tmpType;
						// tmp.printBoard();
						int score = getLocalScore(tmp, i, j, tmp.getBoard()[i][j]);
						score += getLocalScore(tmp, i + 1, j, tmp.getBoard()[i + 1][j]);
						if (bestScore < score) {
							m.score = score;
							m.direction = dir;
							m.x = i;
							m.y = j;
							bestScore = score;
							// tmp.printBoard();
						}
					}
				}
			}
		}
		return m;
	}

	public static int findPath(State s, int x, int y) {
		int type = s.getBoard()[x][y];
		s.getMark()[x][y] = 1;
		// int markScore = markCloseValues(s, x, y, type);
		int score = getLocalScore(s, x, y, type);

		return score;
	}

	private static int getLocalScore(State s, int x, int y, int type) {
		int minx = getMinX(s, x, y, type);
		int miny = getMinY(s, x, y, type);
		int score = 0;

		int scorex = 0;
		int tmpx = minx;
		do {
			scorex++;
			tmpx += 1;
		} while (tmpx < 8 && s.getBoard()[tmpx][y] == type);
		if (scorex < 3) {
			scorex = 0;
		}else if(scorex == 4){
			scorex *= 2;
		}else if(scorex > 5){
			scorex *= 3;
		}
		if(scorex > 2){
			scorex += type;
		}

		int tmpy = miny;
		int scorey = 0;
		do {
			scorey++;
			tmpy += 1;
		} while (tmpy < 8 && s.getBoard()[x][tmpy] == type);
		if (scorey < 3) {
			scorey = 0;
		}else if(scorey == 4){
			scorey *= 2;
		}else if(scorey > 5){
			scorey *= 3;
		}		
		if(scorey > 2){
			scorey += type;
		}
		score = scorex + scorey;

		return score;
	}

	private static int getMinY(State s, int x, int y, int type) {
		if (y - 1 >= 0 && s.getBoard()[x][y - 1] == type) {
			return getMinY(s, x, y - 1, type);
		}
		return y;
	}

	private static int getMinX(State s, int x, int y, int type) {
		if (x - 1 >= 0 && s.getBoard()[x - 1][y] == type) {
			return getMinX(s, x - 1, y, type);
		}
		return x;
	}

	public static int markCloseValues(State s, int x, int y, int type) {
		int score = 0;
		if (x - 1 > 0 && s.getMark()[x - 1][y] == 0 && s.getBoard()[x - 1][y] == type) {
			s.getMark()[x - 1][y] = 1;
			score += 1 + markCloseValues(s, x - 1, y, type);
		}
		if (x + 1 <= 8 && s.getMark()[x + 1][y] == 0 && s.getBoard()[x + 1][y] == type) {
			s.getMark()[x + 1][y] = 1;
			score += 1 + markCloseValues(s, x + 1, y, type);
		}
		if (y - 1 > 0 && s.getMark()[x][y - 1] == 0 && s.getBoard()[x][y - 1] == type) {
			s.getMark()[x][y - 1] = 1;
			score += 1 + markCloseValues(s, x, y - 1, type);
		}
		if (y + 1 <= 8 && s.getMark()[x][y + 1] == 0 && s.getBoard()[x][y + 1] == type) {
			s.getMark()[x][y + 1] = 1;
			score += 1 + markCloseValues(s, x, y + 1, type);
		}
		return score;
	}
}
