package com.dmajewski.gow.img;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import com.dmajewski.gow.game.State;

public class ScreenProcessor {

	public static int PIXEL_SIZE = 118;

	public static void pixelateWindowsScreen(BufferedImage bufferedImage, State s, double xImgFix, double yImgFix)
			throws IOException {
		int pixelSize = PIXEL_SIZE + 2;
//		File outputFile = Files.createTempFile("screen", ".png").toFile();
		// get a BufferedImage object from file

		// loop through the image and produce squares pixelSize*pixelSize
		int i = 0, j = 0;
		for (int w = (int) (469 * xImgFix); i < 8; w += Math.round(pixelSize * xImgFix)) {
			j = 0;
			for (int h = (int) (63 * yImgFix); j < 8; h += Math.round(pixelSize * yImgFix)) {
				// new Color(bufferedImage.getRGB(w, h));
				Color pixelColor = averageColor(bufferedImage, (int) Math.round(w + 20 * xImgFix),
						(int) Math.round(h + 48 * yImgFix), (int) Math.round(74 * xImgFix),
						(int) Math.round(48 * yImgFix));
//				 System.out.println(Math.sqrt(Math.pow(pixelColor.getRed(), 2)
//				 + Math.pow(pixelColor.getGreen(), 2) +
//				 Math.pow(pixelColor.getBlue(), 2)));
				;
//				 System.out.print(Integer.toBinaryString(pixelColor.getRGB()) + " ");
				 
				 float [] hsb = Color.RGBtoHSB(pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue(), null);
//				 System.out.print(Arrays.toString(hsb) + "  ");
				s.getBoard()[j][i] = categorizeColour(hsb);
				s.getBoardColors()[j][i] = hsb;
				Graphics graphics = bufferedImage.getGraphics();
				graphics.setColor(pixelColor);
				graphics.fillRect(w, h, (int)Math.round(pixelSize * xImgFix), (int)Math.round(pixelSize* yImgFix));
				j++;
			}
//			 System.out.println();
			i++;
		}

//		// output file
//		ImageIO.write(bufferedImage, "png", outputFile);
//		System.out.println(outputFile.getCanonicalPath());

		// s.printBoard();
	}

	public static void pixelateScreen(BufferedImage bufferedImage, State s) throws IOException {
		int pixelSize = PIXEL_SIZE;
		File outputFile = Files.createTempFile("screen", ".png").toFile();
		// get a BufferedImage object from file

		// loop through the image and produce squares pixelSize*pixelSize
		int i = 0, j = 0;
		for (int w = 485; w < 485 + (pixelSize * 8); w += pixelSize) {
			j = 0;
			for (int h = 79; h < 79 + (pixelSize * 8); h += pixelSize) {
				// new Color(bufferedImage.getRGB(w, h));
				Color pixelColor = averageColor(bufferedImage, w + 20, h + 48, 74, 48);
				// System.out.println(Math.sqrt(Math.pow(pixelColor.getRed(), 2)
				// + Math.pow(pixelColor.getGreen(), 2) +
				// Math.pow(pixelColor.getBlue(), 2)));
				;
				// System.out.print(Integer.toBinaryString(pixelColor.getRGB())
				// + " ");
				 float [] hsb = Color.RGBtoHSB(pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue(), null);
//				 System.out.println(hsb + "  ");
				s.getBoard()[j][i] = categorizeColour(hsb);
				Graphics graphics = bufferedImage.getGraphics();
				graphics.setColor(pixelColor);
				graphics.fillRect(w, h, pixelSize, pixelSize);
				j++;
			}
//			 System.out.println();
			i++;
		}

		// output file
		ImageIO.write(bufferedImage, "png", outputFile);
		System.out.println(outputFile.getCanonicalPath());
		// s.printBoard();
	}

	public static Color averageColor(BufferedImage bi, int x0, int y0, int w, int h) {
		int x1 = x0 + w;
		int y1 = y0 + h;
		long sumr = 0, sumg = 0, sumb = 0;
		for (int x = x0; x < x1; x++) {
			for (int y = y0; y < y1; y++) {
				Color pixel = new Color(bi.getRGB(x, y));
				sumr += pixel.getRed();
				sumg += pixel.getGreen();
				sumb += pixel.getBlue();
			}
		}
		int num = w * h;
		return new Color((float) (sumr / num / 256.0), (float) (sumg / num / 256.0), (float) (sumb / num / 256.0));
	}

	/**
	 * 0 - bronze 1 - silver 2 - gold 
	 * 3 - bag 
	 * 4 - bronze chest 
	 * 5 - green chest 
	 * 6 - red chest 
	 * 7 - safe
	 * 9 - no gem
	 * @param value
	 * @return
	 */
	public static int categorizeColour(float [] hsb) {
		float value = hsb[0];
		if(value < 0.02){
			return 9;// no gem
		}else if (value > 0.031 && value < 0.0354609) {
			if(hsb[1] < 0.464){
				return 3;
			}
			return 0;
		} else if (value > 0.53 && value < 0.56) {
			return 1;
		} else if (value > 0.1 && value < 0.2) {
			if(hsb[1] > 0.22 && hsb[1] < 0.23){
				return 9;
			}
			return 2;
		} else if (value > 0.0354609 && value < 0.057) {
			if(hsb[1] > 0.57){
				return 0;
			}else if(hsb[1] < 0.51){
				return 3;
			}else{
				return 7;
			}
		} else if (value > 0.054 && value < 0.0651) {
			if(hsb[1] > 0.53){
				return 7;
			}
			return 4;
		} else if (value > 0.2 && value < 0.3) {
			return 5;
		} else if (value > 0.018 && value < 0.0325) {
			return 6;
		} else if (value > 0.0651 && value < 0.075) {
			return 7;
		}else if (value > 0.57 && value < 0.85){
			//blue ribbon, also means end of game
			return 9;
		}
//		System.out.println("Unknown color: " + value);
		return -1;
	}

}
