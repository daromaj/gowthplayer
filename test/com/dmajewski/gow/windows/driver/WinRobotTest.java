package com.dmajewski.gow.windows.driver;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import com.dmajewski.gow.game.State;
import com.dmajewski.gow.game.WindowsPlayer;
import com.dmajewski.gow.img.ScreenProcessor;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.shapes.ellipse.BinaryEllipseDetector;
import boofcv.factory.shape.FactoryShapeDetector;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import georegression.struct.shapes.EllipseRotated_F64;

public class WinRobotTest {

	@Test
	public void testTakeScreenshotToFile() {
		WinRobot.takeScreenshot("test"+(new Date().getTime())+".bmp");
	}
	
	@Test
	public void testSwipe() throws Exception {
		WinRobot.takeScreenshot();
		WinRobot.focusOnWindow();
		WinRobot.swipe(0, 0, 0);
	}
	
	@Test
	public void testFocus() throws Exception {
		WinRobot.focusOnWindow();
	}
	
	@Test
	public void testClick() throws Exception {
		WinRobot.takeScreenshot();
		WinRobot.focusOnWindow();
		//forward
		System.out.println("About to click");
		//WinRobot.click(950, 800);
//		Thread.sleep(2000);
		Thread.sleep(1000);
		// mini games
		WinRobot.click(575, 972);

		Thread.sleep(1000);
		// mini treasure hunt
//		WinRobot.click(1033, 346);
//		Thread.sleep(2000);
//		//mini games
//		WinRobot.click(550, 1000);
//
//		Thread.sleep(1000);
//		//mini treasure hunt
//		WinRobot.click(550, 850);
//
//		Thread.sleep(1000);
//		//start treasure hunt
//		WinRobot.click(960, 900);
	
	}	

	@Test
	public void testMousePosition() throws Exception {
		WinRobot.takeScreenshot();
		while(true){
			Point p = MouseInfo.getPointerInfo().getLocation();
			System.out.println(p.x - WinRobot.bounds.left + ", " + (p.y - WinRobot.bounds.top - WinRobot.BAR_HEIGHT));
			Thread.sleep(500);
		}
	}
	
	@Test
	public void testBuyMaps() throws Exception {
		WinRobot.takeScreenshot();		
		WindowsPlayer.buyMaps(1);
	}
	
	@Test
	public void testReadBoard() throws Exception {
//		BufferedImage bufferedImage = WinRobot.takeScreenshot().getSubimage(0, 0, (int) Math.round(Paint.windowWidth / WinRobot.ZOOM), (int) Math.round(Paint.windowHeight / WinRobot.ZOOM));
		BufferedImage bufferedImage = WinRobot.takeScreenshot().getSubimage(0, 0, (int) Math.round(Paint.windowWidth), (int) Math.round(Paint.windowHeight));
//		double xImgFix = WinRobot.xFix / WinRobot.ZOOM;
//		double yImgFix = WinRobot.yFix / WinRobot.ZOOM;
		double xImgFix = WinRobot.xFix;
		double yImgFix = WinRobot.yFix;
		State s = new State();
		ScreenProcessor.pixelateWindowsScreen(bufferedImage, s, xImgFix, yImgFix);
		System.out.println(s.printBoard());
		;
		
	}
	
	@Test
	public void testFindEndState() throws Exception {
		BufferedImage img = ImageIO.read(new File("end1.bmp"));
		State state = new State();
		ScreenProcessor.pixelateWindowsScreen(img, state, 1, 1);
	}
	
	@Test
	public void testIsMainScreenActive() throws Exception {
		System.out.println(WindowsPlayer.isMainScreenActive());
	}
	
	
	@Test
	public void testGetNumberOfRoundsLeft() throws Exception {
		System.out.println(WindowsPlayer.noMoreMovesLeft());
	}
	
	public static void main(String[] args) throws Exception {
		testShapesDetection();
	}
	
	public static void testShapesDetection() throws Exception{
		BinaryEllipseDetector<GrayU8> ellipseDetector = FactoryShapeDetector.ellipse(null,
				GrayU8.class);		
		
		//entire screen
//		BufferedImage image = WinRobot.takeScreenshot().getSubimage(0, 0, (int) Math.round(Paint.windowWidth), (int) Math.round(Paint.windowHeight));
		
		//lifes
		BufferedImage image = WinRobot.takeScreenshot().getSubimage((int) Math.round(278 * WinRobot.xFix),
				(int) Math.round(162 * WinRobot.yFix), (int) Math.round(66 * WinRobot.xFix),
				(int) Math.round(42 * WinRobot.yFix));		
		
		//below is to identify main screen
//		BufferedImage image = WinRobot.takeScreenshot()
//				.getSubimage((int) Math.round(695 * WinRobot.xFix),
//				(int) Math.round(1012 * WinRobot.yFix), 
//				(int) Math.round(110 * WinRobot.xFix),
//				(int) Math.round(40 * WinRobot.yFix));

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
		List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label);

		 System.out.println(ellipseFound.size);
		 System.out.println(contours.size());
		
		Graphics2D g2 = image.createGraphics();
		g2.setStroke(new BasicStroke(3));
		g2.setColor(Color.RED);
		for (int i = 0; i < ellipseFound.size; i++) {
			VisualizeShapes.drawEllipse(ellipseFound.get(i), g2);
		}
		ListDisplayPanel panel = new ListDisplayPanel();
		panel.addImage(image,File.createTempFile("test", ".bmp").getName());
		
		ShowImages.showWindow(panel,"Detected Ellipses",true);
	}
		
}
