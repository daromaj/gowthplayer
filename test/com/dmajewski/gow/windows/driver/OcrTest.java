package com.dmajewski.gow.windows.driver;

import org.junit.Test;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

public class OcrTest {

	@Test
	public void testSomeOcr() throws Exception {
		ITesseract instance = new Tesseract();

		System.out.println(instance.doOCR(WinRobot.takeScreenshot().getSubimage(
				(int) Math.round(1054 * WinRobot.xFix),
				(int) Math.round(1003 * WinRobot.yFix), 
				(int) Math.round(91 * WinRobot.xFix),
				(int) Math.round(33 * WinRobot.yFix))));
	}

}
