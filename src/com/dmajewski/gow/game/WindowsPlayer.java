package com.dmajewski.gow.game;

import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.ddogleg.struct.FastQueue;

import com.dmajewski.gow.img.ScreenProcessor;
import com.dmajewski.gow.windows.driver.Paint;
import com.dmajewski.gow.windows.driver.WinRobot;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.shapes.ellipse.BinaryEllipseDetector;
import boofcv.factory.shape.ConfigEllipseDetector;
import boofcv.factory.shape.FactoryShapeDetector;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import georegression.struct.shapes.EllipseRotated_F64;

public class WindowsPlayer implements Runnable {

	public boolean suspended = false;
	public int gamesToPlay = 1;

	private Consumer<WindowsPlayer> finishCallback;
	private Consumer<WindowsPlayer> singleGameFinishCallback;
	private Consumer<State> stateChangeCallback;
	private Consumer<Move> bestMoveCallback;
	private Consumer<Exception> exceptionCallback;
	private Consumer<String> infoCallback;

	private void info(String s) {
		if (infoCallback != null) {
			if (".".equals(s)) {
				infoCallback.accept(s);
			} else {
				infoCallback.accept(s + "\n");
			}
		}
	}

	public WindowsPlayer() {
	}

	private static final ConfigEllipseDetector ellipseConfig = new ConfigEllipseDetector();
	static {
		ellipseConfig.processInternal = true;
	}
	private static final BinaryEllipseDetector<GrayU8> ellipseDetector = FactoryShapeDetector.ellipse(null,
			GrayU8.class);

	public static boolean isMainScreenActive() {
		BufferedImage image = WinRobot.takeScreenshot().getSubimage((int) Math.round(695 * WinRobot.xFix),
				(int) Math.round(1012 * WinRobot.yFix), (int) Math.round(110 * WinRobot.xFix),
				(int) Math.round(40 * WinRobot.yFix));

		GrayU8 input = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
		GrayU8 binary = new GrayU8(input.width, input.height);
		GrayS32 label = new GrayS32(input.width, input.height);
		int threshold = GThresholdImageOps.computeOtsu(input, 0, 255);
		ThresholdImageOps.threshold(input, binary, threshold, true);
		BinaryImageOps.invert(binary, binary);

		GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
		filtered = BinaryImageOps.dilate8(filtered, 1, null);

		ellipseDetector.process(input, filtered);

		FastQueue<EllipseRotated_F64> ellipseFound = ellipseDetector.getFoundEllipses();

//		System.out.println(ellipseFound.size);
		
		List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label);
		
//		System.out.println(contours.size());

		return ellipseFound.size == 4 && contours.size() == 8;
	}

	public static boolean noMoreMovesLeft(BufferedImage... img) {
		if (img == null || img.length == 0) {
			img = new BufferedImage[] { WinRobot.takeScreenshot() };
		}
		BufferedImage image = img[0].getSubimage((int) Math.round(278 * WinRobot.xFix),
				(int) Math.round(162 * WinRobot.yFix), (int) Math.round(66 * WinRobot.xFix),
				(int) Math.round(42 * WinRobot.yFix));	
		GrayU8 input = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
		GrayU8 binary = new GrayU8(input.width, input.height);
		GrayS32 label = new GrayS32(input.width, input.height);
		int threshold = GThresholdImageOps.computeOtsu(input, 0, 255);
		ThresholdImageOps.threshold(input, binary, threshold, true);
		binary = BinaryImageOps.invert(binary, binary);

		GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
		filtered = BinaryImageOps.dilate8(filtered, 1, null);

		ellipseDetector.process(input, filtered);

		FastQueue<EllipseRotated_F64> ellipseFound = ellipseDetector.getFoundEllipses();

		List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label);

		boolean hasOneInternal = false;

		if (contours.size() == 1) {
			hasOneInternal = contours.get(0).internal != null && !contours.get(0).internal.isEmpty()
					&& contours.get(0).internal.size() == 1;
		}
		return contours.size() == 1 && hasOneInternal && ellipseFound.size == 1;
	}

	public void startNewGame() throws Exception {
		if (isMainScreenActive()) {
			info("Starting new game...");
			WinRobot.focusOnWindow();
			// forward
			// WinRobot.click(1850, 1000);
			// Thread.sleep(500);
			// WinRobot.click(1850, 1000);
			Thread.sleep(1000);
			// mini games
			WinRobot.click(575, 972);

			Thread.sleep(1000);
			// mini treasure hunt
			WinRobot.click(1033, 346);

			//Thread.sleep(1000);
			// start treasure hunt
			//WinRobot.click(950, 900);
			Thread.sleep(2000);
		}
	}

	boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
		if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
			for (int x = 0; x < img1.getWidth(); x++) {
				for (int y = 0; y < img1.getHeight(); y++) {
					if (img1.getRGB(x, y) != img2.getRGB(x, y))
						return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	public void play() throws Exception {
		// Color avgNumber = new Color(0, 0, 0);
		int exceptionCount = 0;
		int noMove = 0;
		BufferedImage prevRoundNumber = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

		startNewGame();
		int endGame = 0;
		do {
			if (suspended || gamesToPlay <= 0) {
				return;
			}

			// WinRobot.focusOnWindow();
			State initialState = new State();
			// String fileName = new Date().getTime() + "_temp.png";
			// WinRobot.takeScreenshot(fileName);
			// File f = new File(fileName);
			// BufferedImage bufferedImage = ImageIO.read(f);
			BufferedImage bufferedImage = WinRobot.takeScreenshot().getSubimage(0, 0,
					(int) Math.round(Paint.windowWidth),
					(int) Math.round(Paint.windowHeight));
			double xImgFix = WinRobot.xFix;
			double yImgFix = WinRobot.yFix;
			BufferedImage roundNumber = bufferedImage.getSubimage((int) Math.round(1090 * xImgFix),
					(int) Math.round(15 * yImgFix), (int) Math.round(80 * xImgFix), (int) Math.round(40 * yImgFix));
			// Color numColor = ScreenProcessor.averageColor(bufferedImage,
			// (int) Math.round(1040 * xImgFix),
			// (int) Math.round(15 * yImgFix), (int) Math.round(70 * xImgFix),
			// (int) Math.round(35 * yImgFix));
			// avgNumber = numColor;

			// ImageIO.write(roundNumber, "png", Files.createTempFile("num",
			// ".png").toFile());
			if (noMoreMovesLeft(bufferedImage)) {
				endGame = 2;
			}

			if (endGame < 2 && !bufferedImagesEqual(roundNumber, prevRoundNumber)) {
				ScreenProcessor.pixelateWindowsScreen(bufferedImage, initialState, xImgFix, yImgFix);
				try {
					endGame = 0;
					noMove = 0;
					initialState.printBoard();
					if (stateChangeCallback != null) {
						stateChangeCallback.accept(initialState);
					}
					Move m = getBestMove(initialState);
					// System.out.println("Best move: " + m);
					if (bestMoveCallback != null) {
						bestMoveCallback.accept(m);
					}
					WinRobot.swipe(m.x, m.y, m.direction);

					exceptionCount = 0;
					prevRoundNumber = roundNumber;
					Thread.sleep(500);
				} catch (Exception e) {
					// e.printStackTrace();
					exceptionCount++;
					Thread.sleep(500);
				}

			} else {
				noMove++;
				info(".");
				Thread.sleep(200);
			}
			if (noMove > 9) {
				prevRoundNumber = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
				noMove = 0;
				//sometimes Player get's blocked because there was a click registered in wrong place
				//we make a random click to be able to move on
				WinRobot.clickRandomGem();
			}

			if (exceptionCount > 10) {
				int[] pos = initialState.getUnknownGemPosition();
				String message = "I can't regonize gem at: " + pos[0] + "," + pos[1] + " color: "
						+ Arrays.toString(initialState.getBoardColors()[pos[0]][pos[1]]);
				info(message);
				throw new RuntimeException(message);
			}

			if (endGame > 1) {
				info("No more moves");
				exceptionCount = 0;
				endGame = 0;
				int finishCounter = 5;
				do {
					info("Going back to main screen");
					if(finishCounter % 2 == 0) {
						WinRobot.click(575, 972);
					}else {
						WinRobot.click(622, 1005);
					}
					//622,1005
//					WinRobot.click(979, 1005);
					Thread.sleep(2000);
					finishCounter--;
				} while (!isMainScreenActive() && finishCounter > 0);
				if(finishCounter<=0){
					info("Can't go back to main screen. I'm giving up...");
				}
				gamesToPlay -= 1;
				if (singleGameFinishCallback != null) {
					singleGameFinishCallback.accept(this);
				}
				if (gamesToPlay > 0) {
					startNewGame();
				}
			}

		} while (true);
	}

	public static Move getBestMove(State s) throws CloneNotSupportedException {
		Move m = new Move();
		int bestScore = 0;
		for (int i = 0; i < 8; i++) {// row
			for (int j = 0; j < 8; j++) {// column
				for (int dir = 0; dir < 2; dir++) {
					if (dir == 0) {// right
						if (j + 1 > 7 || s.getBoard()[i][j] == s.getBoard()[i][j + 1] || s.getBoard()[i][j] > 6
								|| s.getBoard()[i][j + 1] > 6) {
							continue;
						}
						State tmp = s.clone();
						int tmpType = tmp.getBoard()[i][j];
						tmp.getBoard()[i][j] = tmp.getBoard()[i][j + 1];
						tmp.getBoard()[i][j + 1] = tmpType;

						int score = getScore(tmp, i, j, dir, true);
						if (bestScore < score) {
							m.score = score;
							m.direction = dir;
							m.x = i;
							m.y = j;
							bestScore = score;
							// tmp.printBoard();
						}
					} else {// down
						if (i + 1 > 7 || s.getBoard()[i][j] == s.getBoard()[i + 1][j] || s.getBoard()[i][j] > 6
								|| s.getBoard()[i + 1][j] > 6) {
							continue;
						}
						State tmp = s.clone();
						int tmpType = tmp.getBoard()[i][j];
						tmp.getBoard()[i][j] = tmp.getBoard()[i + 1][j];
						tmp.getBoard()[i + 1][j] = tmpType;
						// tmp.printBoard();
						int score = getScore(tmp, i, j, dir, true);

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

	private static int getScore(State tmp, int i, int j, int dir, boolean withNextState) {
		int newi = i;
		int newj = j;
		if (dir == 0) {
			newj = j + 1;
		} else {
			newi = i + 1;
		}
		int score1 = getLocalScore(tmp, i, j, tmp.getBoard()[i][j]);
		int score2 = getLocalScore(tmp, newi, newj, tmp.getBoard()[newi][newj]);
		if (score1 > 0 && score2 == 0) {
			int score3 = 0;
			do {
				score3 = 0;
				upgradeGem(tmp, i, j);
				score3 = getLocalScore(tmp, i, j, tmp.getBoard()[i][j]);
				if (score3 > 0) {
					// tmp.printBoard();
					score1 += score3;
				}
			} while (score3 > 0);
		} else if (score2 > 0 && score1 == 0) {
			int score3 = 0;
			do {
				score3 = 0;
				upgradeGem(tmp, newi, newj);
				score3 = getLocalScore(tmp, newi, newj, tmp.getBoard()[newi][newj]);
				if (score3 > 0) {
					// tmp.printBoard();
					score2 += score3;
				}
			} while (score3 > 0);
		} else if (score1 > 0 && score2 > 0) {
			upgradeGem(tmp, i, j);
			upgradeGem(tmp, newi, newj);
			score1 += getScore(tmp, i, j, dir, false);
		}
		if (withNextState && (score1 > 0 || score2 > 0)) {
			State nexts = getNextState(tmp);
			int nsScore = findMaxPointsOnBoard(nexts);
			score1 += nsScore;
		}

		return score1 + score2;
	}

	private static int findMaxPointsOnBoard(State tmp) {
		int maxScore = 0;
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				int type = tmp.getBoard()[i][j];
				if (type < 0)
					continue;
				int score = getLocalScore(tmp, i, j, type);
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}

		return maxScore;
	}

	private static State getNextState(State tmp) {
		// System.out.println("Before state");
		// tmp.printBoard();
		try {
			State s = tmp.clone();
			for (int j = 0; j < 8; j++) {
				for (int i = 7; i > 0; i--) {
					int type = s.getBoard()[i][j];
					if (type >= 0) {
						continue;
					}
					// type is -9
					if (i == 0) {
						continue;
					}
					for (int k = i - 1; k >= 0; k--) {
						if (s.getBoard()[k][j] >= 0) {
							s.getBoard()[i][j] = s.getBoard()[k][j];
							s.getBoard()[k][j] = -9;
							break;
						}
					}

				}
			}
			// System.out.println("After state");
			// s.printBoard();
			return s;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void upgradeGem(State s, int i, int j) {
		int type = s.getBoard()[i][j];
		int minx = getMinX(s, i, j, type);
		int miny = getMinY(s, i, j, type);
		int maxx = getMaxX(s, i, j, type);
		int maxy = getMaxY(s, i, j, type);
		if (maxx - minx > 1) {
			for (int k = minx; k <= maxx; k++) {
				s.getBoard()[k][j] = -9;
			}
		}
		if (maxy - miny > 1) {
			for (int k = miny; k <= maxy; k++) {
				s.getBoard()[i][k] = -9;
			}
		}
		s.getBoard()[i][j] = ++type;
		// s.printBoard();
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
		}

		int tmpy = miny;
		int scorey = 0;
		do {
			scorey++;
			tmpy += 1;
		} while (tmpy < 8 && s.getBoard()[x][tmpy] == type);
		if (scorey < 3) {
			scorey = 0;
		}
		score = scorex + scorey;

		if (score == 3) {
			// max: 3 + 8 + 7 = 18
			score += x;
		} else if (score == 4) {
			score *= 10;
			score += (8 - x);
		} else if (score >= 5) {
			score *= 20;
			score += (8 - x);
		}

		return score;
	}

	private static int getMinY(State s, int x, int y, int type) {
		if (y - 1 >= 0 && s.getBoard()[x][y - 1] == type) {
			return getMinY(s, x, y - 1, type);
		}
		return y;
	}

	private static int getMaxY(State s, int x, int y, int type) {
		if (y + 1 < 8 && s.getBoard()[x][y + 1] == type) {
			return getMaxY(s, x, y + 1, type);
		}
		return y;
	}

	private static int getMinX(State s, int x, int y, int type) {
		if (x - 1 >= 0 && s.getBoard()[x - 1][y] == type) {
			return getMinX(s, x - 1, y, type);
		}
		return x;
	}

	private static int getMaxX(State s, int x, int y, int type) {
		if (x + 1 < 8 && s.getBoard()[x + 1][y] == type) {
			return getMaxX(s, x + 1, y, type);
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

	@Override
	public void run() {
		try {
			play();
		} catch (Exception e) {
			if (getExceptionCallback() != null) {
				getExceptionCallback().accept(e);
			}
		}
		if (finishCallback != null) {
			finishCallback.accept(this);
		}
	}

	public Consumer<WindowsPlayer> getFinishCallback() {
		return finishCallback;
	}

	public void setFinishCallback(Consumer<WindowsPlayer> finishCallback) {
		this.finishCallback = finishCallback;
	}

	public Consumer<State> getStateChangeCallback() {
		return stateChangeCallback;
	}

	public void setStateChangeCallback(Consumer<State> stateChangeCallback) {
		this.stateChangeCallback = stateChangeCallback;
	}

	public Consumer<Move> getBestMoveCallback() {
		return bestMoveCallback;
	}

	public void setBestMoveCallback(Consumer<Move> bestMoveCallback) {
		this.bestMoveCallback = bestMoveCallback;
	}

	public Consumer<WindowsPlayer> getSingleGameFinishCallback() {
		return singleGameFinishCallback;
	}

	public void setSingleGameFinishCallback(Consumer<WindowsPlayer> singleGameFinishCallback) {
		this.singleGameFinishCallback = singleGameFinishCallback;
	}

	public static void buyMaps(int count) throws Exception {
		if(!isMainScreenActive()){
			//info("Not on main screen! Can't buy maps.");
			return;
		}
		WinRobot.focusOnWindow();
		int titlebar = WinRobot.BAR_HEIGHT;
		Thread.sleep(100);
		// Sklep - 1835, 1040
		WinRobot.click(1835, 970 + titlebar);
		Thread.sleep(1000);
		// Glory rewards - 1380, 270
		WinRobot.click(1380, 200 + titlebar);
		Thread.sleep(1000);
		for (int i = 0; i < count; i++) {
			// Mapy - 780, 590
			WinRobot.click(700, 520 + titlebar);
			Thread.sleep(1000);
			// Kup - 970, 920
			WinRobot.click(970, 850 + titlebar);
			Thread.sleep(1000);
			// Potwierdz - 955, 575
			WinRobot.click(955, 505 + titlebar);
			Thread.sleep(1000);
			// Zamknij - 1540, 180
			WinRobot.click(1540, 110 + titlebar);
			Thread.sleep(1000);
		}

		// Koniec kupowania - 1850, 140
		Thread.sleep(1000);
		WinRobot.click(1850, 70 + titlebar);

	}

	public Consumer<Exception> getExceptionCallback() {
		return exceptionCallback;
	}

	public void setExceptionCallback(Consumer<Exception> exceptionCallback) {
		this.exceptionCallback = exceptionCallback;
	}

	public Consumer<String> getInfoCallback() {
		return infoCallback;
	}

	public void setInfoCallback(Consumer<String> infoCallback) {
		this.infoCallback = infoCallback;
	}
}
