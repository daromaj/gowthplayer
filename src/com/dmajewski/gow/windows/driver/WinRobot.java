package com.dmajewski.gow.windows.driver;

import java.awt.AWTException;
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
	
	public static double ZOOM = 1.0d;
	
	public static double xFix;
	public static double yFix;
	
	private static HWND hWnd = User32.INSTANCE.FindWindow(null, "GemsofWar");
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
	}
	
	public static BufferedImage takeScreenshot() {
		BufferedImage result = Paint.capture(gethWnd());
        bounds = new RECT();
        User32Extra.INSTANCE.GetWindowRect(gethWnd(), bounds);	
        RECT tmpBounds = new RECT();
        User32Extra.INSTANCE.GetClientRect(gethWnd(), tmpBounds);
        BAR_HEIGHT = bounds.bottom - bounds.top - tmpBounds.bottom - 16;
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
        bounds = new RECT();
        User32Extra.INSTANCE.GetWindowRect(gethWnd(), bounds);
        
		robot.mouseMove(bounds.left + (int)Math.round(x * xFix), bounds.top + (int)Math.round(y*yFix));
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		
	}

	public static void swipe(int x, int y, int direction) {
        
//        System.out.println(xFix);
//        System.out.println(yFix);
        
        int startX, startY, endX, endY;
		if (direction == 0) {//right
			startX = (int) ((544 + (118 * y)) * xFix);
			startY = (int) ((140 + (118 * x)) * yFix);
			endX = (int) ((544 + (118 * y) + 118) * xFix);
			endY = (int) ((140 + (118 * x)) * yFix);
		} else {//down
			startX = (int) ((544 + (118 * y)) * xFix);
			startY = (int) ((140 + (118 * x)) * yFix);
			endX = (int) ((544 + (118 * y)) * xFix);
			endY = (int) ((140 + (118 * x) + 118) * yFix);
		}
		startX += bounds.left;
		startY += bounds.top + BAR_HEIGHT;
		endX += bounds.left;
		endY += bounds.top + BAR_HEIGHT;
//        System.out.println("startX " + startX);
//        System.out.println("startY " + startY);
//        System.out.println("endX " + endX);
//        System.out.println("endY " + endY);
        long delay = 100;
		robot.mouseMove(startX, startY);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		robot.mouseMove(endX, endY);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
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
