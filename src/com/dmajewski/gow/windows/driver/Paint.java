package com.dmajewski.gow.windows.driver;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

import jna.extra.User32Extra;

public class Paint{
	
	//private static final HWND desktop = User32.INSTANCE.GetDesktopWindow();
	
	private static Robot robot;
	
	private static Robot getRobot(){
		if (robot == null) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
		return robot;
	}
	
	public static int windowWidth;
	public static int windowHeight;

    public static BufferedImage capture(HWND hWnd) {

//        HDC hdcWindow = User32.INSTANCE.GetDC(hWnd);
//        HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);

        RECT windowBounds = new RECT();
        User32Extra.INSTANCE.GetWindowRect(hWnd, windowBounds);
        
        RECT clientBounds = new RECT();
        User32Extra.INSTANCE.GetClientRect(hWnd, clientBounds);        

        int width = clientBounds.right - clientBounds.left;
        windowWidth = width;
        int height = clientBounds.bottom - clientBounds.top;
        windowHeight = height;
        
        return getRobot().createScreenCapture(new Rectangle((windowBounds.right - clientBounds.right),(windowBounds.bottom - clientBounds.bottom), width, height));
        
//        System.out.println("Window width: " + width);
//        System.out.println("Window height: " + height);

//        HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);
//        
//
//        HANDLE hOld = GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);
//        GDI32Extra.INSTANCE.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, 0, 0, WinGDIExtra.SRCCOPY);
//
//        GDI32.INSTANCE.SelectObject(hdcMemDC, hOld);
//        GDI32.INSTANCE.DeleteDC(hdcMemDC);
//
//        BITMAPINFO bmi = new BITMAPINFO();
//        bmi.bmiHeader.biWidth = width;
//        bmi.bmiHeader.biHeight = -height;
//        bmi.bmiHeader.biPlanes = 1;
//        bmi.bmiHeader.biBitCount = 32;
//        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;
//
//        Memory buffer = new Memory(width * height * 4);
//        GDI32.INSTANCE.GetDIBits(hdcWindow, hBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);
//
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        image.setRGB(0, 0, width, height, buffer.getIntArray(0, width * height), 0, width);
//
//        GDI32.INSTANCE.DeleteObject(hBitmap);
//        User32.INSTANCE.ReleaseDC(hWnd, hdcWindow);

//        return image;

    }

    public static void main(String[] args) {
        HWND hWnd = User32.INSTANCE.FindWindow(null, "GemsofWar");
        capture(hWnd);
    }

}