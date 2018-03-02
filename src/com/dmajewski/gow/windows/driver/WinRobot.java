package com.dmajewski.gow.windows.driver;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

import javafx.stage.Stage;
import jna.extra.User32Extra;

public class WinRobot {
	
	public static Robot robot;
	
	public static final int DEFAULT_WIDTH = 1920;
	public static final int DEFAULT_HEIGHT = 1080;
	
	public static int BAR_HEIGHT = 70;
	
	public static double ZOOM = 2.5d;
	
	public static double xFix;
	public static double yFix;
	
	private static HWND hWnd = null;//User32.INSTANCE.FindWindow(null, "GemsofWar");
	public static RECT bounds;
	
	public static double findZoom(Stage stage){
		if(stage.isShowing()){
			HWND handler = User32.INSTANCE.FindWindow(null, stage.getTitle());
			RECT rect = new RECT();
	        User32Extra.INSTANCE.GetClientRect(handler, rect);
	        double originalWidth = stage.getScene().getWidth();
	        double screenWidth = rect.right - rect.left;
	        double result = screenWidth / originalWidth;
	        ZOOM = result;
	        return result;
		}
		return 1;
	}
	
	public static void setZoom(double value){
		ZOOM = value;
	}
	
	static{
        try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public static void takeScreenshot(String fname) {
	    File outputfile = new File(fname);
	    try {
			ImageIO.write(takeScreenshot(), "bmp", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    System.out.println("Screenshot saved as: " + outputfile.getAbsolutePath());
	}
	
	public static BufferedImage takeScreenshot() {
		BufferedImage result = Paint.capture(gethWnd());
        bounds = new RECT();
        User32Extra.INSTANCE.GetWindowRect(gethWnd(), bounds);	
//        System.out.println("Window bounds: " + bounds);
        RECT tmpBounds = new RECT();
        User32Extra.INSTANCE.GetClientRect(gethWnd(), tmpBounds);
//        System.out.println("Client bounds: " + tmpBounds);
        BAR_HEIGHT = bounds.bottom - tmpBounds.bottom - bounds.top - 16;
        xFix = Paint.windowWidth * 1.0 / DEFAULT_WIDTH;
        yFix = Paint.windowHeight * 1.0 / DEFAULT_HEIGHT;	
        return result;
	}
	
	public static void focusOnWindow() {
		User32Extra.INSTANCE.SetForegroundWindow(gethWnd());
//        bounds = new RECT();
//        User32Extra.INSTANCE.GetWindowRect(hWnd, bounds);
//		robot.mouseMove(bounds.left+200, bounds.top + 20);
//		robot.mousePress(InputEvent.BUTTON1_MASK);
//		robot.mouseRelease(InputEvent.BUTTON1_MASK);
//        try {
//			Thread.sleep(200);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}	
	
	public static void click(int x, int y){
		mouseMove(x,y);
//        System.out.println("After move: " + MouseInfo.getPointerInfo().getLocation());
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
//        System.out.println("after click: " + MouseInfo.getPointerInfo().getLocation());
		
	}
	
	public static void mouseMove(int x, int y) {
        bounds = new RECT();
        //Windows 10 broke mouse move. Just brilliant. Works when adjusted for screen ZOOM
        User32Extra.INSTANCE.GetWindowRect(gethWnd(), bounds);
        int newX = (int) Math.round((bounds.left + (int)Math.round(x * xFix)) / ZOOM);
        int newY = (int) Math.round((bounds.top + (int)Math.round(y*yFix) + BAR_HEIGHT) / ZOOM);;
//        System.out.println("New coordinates: " + newX + "," + newY);
//        System.out.println("New coordinates: " + (newX*ZOOM) + "," + (newY*ZOOM));
        do {
//        	robot.delay(3000);
        	robot.mouseMove(0, 0);
//        	System.out.println("After move: " + MouseInfo.getPointerInfo().getLocation());
//        	robot.delay(1);
        	robot.mouseMove(newX, newY);
//        	System.out.println("After move: " + MouseInfo.getPointerInfo().getLocation());
//        	System.out.println("Distance: " + MouseInfo.getPointerInfo().getLocation().distance(newX*ZOOM, newY*ZOOM));
        }while(MouseInfo.getPointerInfo().getLocation().distance(newX*ZOOM, newY*ZOOM) > 100);
        
	}
	
	public static void clickRandomGem() {
		int x = (int) Math.round(Math.random()*7);
		int y = (int) Math.round(Math.random()*7);		
		int startX, startY;
		startX = (int) ((544 + (118 * y)) * xFix);
		startY = (int) ((22 + (118 * x)) * yFix);
		startX += bounds.left;
		startY += bounds.top + BAR_HEIGHT;
        int delay = 100;
        mouseMove(startX, startY);
        robot.delay(delay);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.delay(delay);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		robot.delay(delay);		
	}

	public static void swipe(int x, int y, int direction) {
        
//        System.out.println(xFix);
//        System.out.println(yFix);
        
        int startX, startY, endX, endY;
		startX = (int) ((544 + (118 * y)) * xFix);
		startY = (int) ((22 + (118 * x)) * yFix);
		if (direction == 0) {//right
			endX = (int) ((544 + (118 * y) + 118) * xFix);
			endY = (int) ((22 + (118 * x)) * yFix);
		} else {//down
			endX = (int) ((544 + (118 * y)) * xFix);
			endY = (int) ((22 + (118 * x) + 118) * yFix);
		}
		startX += bounds.left;
		startY += bounds.top + BAR_HEIGHT;
		endX += bounds.left;
		endY += bounds.top + BAR_HEIGHT;
//        System.out.println("startX " + startX);
//        System.out.println("startY " + startY);
//        System.out.println("endX " + endX);
//        System.out.println("endY " + endY);
        int delay = 50;
        mouseMove(startX, startY);
		robot.delay(delay);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.delay(delay);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		robot.delay(delay);
        mouseMove(endX, endY);
		robot.delay(delay);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.delay(delay);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		robot.delay(delay);
		//move mouse out of the playing board
		mouseMove(0, 0);
	}

	public static HWND gethWnd() {
		if(hWnd == null){
			hWnd = User32.INSTANCE.FindWindow(null, "GemsofWar");
		}
		return hWnd;
	}

	public static void sethWnd(HWND hWnd) {
		WinRobot.hWnd = hWnd;
	}
	
}
