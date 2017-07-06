package com.dmajewski.gow.mobile.driver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

public class AdbExec {

	public static final String ADB_EXE = "%LOCALAPPDATA%/Android/sdk/platform-tools/adb";

	public static void main(String[] args) {
//		exec("devices");
//		// adb shell screencap -p /data/local/tmp/temp.png
//		exec("shell", "screencap", "-p", "/data/local/tmp/temp.png");
//		exec("pull", "/data/local/tmp/temp.png");
//		exec("shell", "rm", "/data/local/tmp/temp.png");
		takeScreenshot("test.png");
	}

	public static void takeScreenshot(String fname) {
		exec("shell", "screencap", "-p", "/data/local/tmp/" + fname);
		exec("pull", "/data/local/tmp/" + fname);
		exec("shell", "rm", "/data/local/tmp/" + fname);
	}

	public static void swipe(int x, int y, int direction) {
		if (direction == 0) {//right
			exec("shell", "input", "touchscreen", "swipe", 
					Integer.toString(544 + (118 * y)),
					Integer.toString(140 + (118 * x)), 
					Integer.toString(544 + (118 * y) + 118),
					Integer.toString(140 + (118 * x)));
		} else {//down
			exec("shell", "input", "touchscreen", "swipe", 
					Integer.toString(544 + (118 * y)),
					Integer.toString(140 + (118 * x)), 
					Integer.toString(544 + (118 * y)),
					Integer.toString(140 + (118 * x) + 118));

		}

	}

	public static void exec(String... commands) {
		System.out.println(ArrayUtils.toString(commands));
		ProcessBuilder pb = new ProcessBuilder(ArrayUtils.addAll(new String[] { "cmd", "/c", ADB_EXE }, commands));
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();
			System.out.println(IOUtils.toString(p.getInputStream(), Charset.defaultCharset()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Stream<byte[]> execToFile(String... commands) {
		System.out.println(ArrayUtils.toString(commands));
		ProcessBuilder pb = new ProcessBuilder(ArrayUtils.addAll(new String[] { "cmd", "/c", ADB_EXE }, commands));
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();
			;
			return Stream.of(IOUtils.toByteArray(p.getInputStream()));
			//return IOUtils.toString(p.getInputStream(), Charset.defaultCharset()).replaceAll("\r", "").getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	
}
