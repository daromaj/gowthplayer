package com.dmajewski.gow.windows.driver;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

import javax.imageio.ImageIO;

import org.junit.Test;

import com.dmajewski.gow.game.State;
import com.dmajewski.gow.game.WindowsPlayer;
import com.dmajewski.gow.img.ScreenProcessor;

public class WinRobotTest {

	@Test
	public void testTakeScreenshotToFile() {
		WinRobot.takeScreenshot("test"+(new Date().getTime())+".png");
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
		WinRobot.click(1830, 1000);
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
		BufferedImage bufferedImage = WinRobot.takeScreenshot().getSubimage(0, 0, (int) Math.round(Paint.windowWidth / WinRobot.ZOOM), (int) Math.round(Paint.windowHeight / WinRobot.ZOOM));
		double xImgFix = WinRobot.xFix / WinRobot.ZOOM;
		double yImgFix = WinRobot.yFix / WinRobot.ZOOM;
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
		System.out.println(new WindowsPlayer().isMainScreenActive());
	}
	
	
	@Test
	public void testGetNumberOfRoundsLeft() throws Exception {
		System.out.println(new WindowsPlayer().getNumberOfRoundsLeft());
	}
		
}
