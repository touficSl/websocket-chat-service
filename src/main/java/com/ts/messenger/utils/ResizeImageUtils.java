package com.ts.messenger.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;

public class ResizeImageUtils {

	public static void resize(int width, int height, File targetFolder,
			String fileName, int extention) throws IOException {
		
		
	
		File file = new File(targetFolder, fileName + ".png");
		BufferedImage inputImage = ImageIO.read(file);
		
		int originalWidth = inputImage.getWidth();
		int originalHeight = inputImage.getHeight();
		int convertedWidth = width;
		int convertedHeight = height;
		float original_rapport = ((float)originalHeight)/originalWidth;
		float targeted_rapport = ((float)height)/width;

		if(original_rapport > targeted_rapport)
		{
			convertedHeight = height;
			convertedWidth = (int)(convertedHeight/original_rapport);
		}
		else
		{
			convertedWidth = width;
			convertedHeight = (int)(convertedWidth * original_rapport);
		}

		width = convertedWidth;
		height = convertedHeight;
		
		if (extention == 1) {
			ResampleOp resampleOp = new ResampleOp(width, height);
			resampleOp
					.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.VerySharp);
			BufferedImage rescaled = resampleOp.filter(inputImage, null);

			ImageIO.write(rescaled, "PNG", file);

		} else {
			BufferedImage outputImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = outputImage.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setColor(Color.white);
			g.fillRect(0, 0, width, height);
			g.drawImage(inputImage, 0, 0, width, height, null);
			g.dispose();
			g.setComposite(AlphaComposite.Src);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			ImageIO.write(outputImage, "png", file);
		}
	}

	/**
	 * Convert Image to BufferedImage.
	 * 
	 * @param image
	 *            Image to be converted to BufferedImage.
	 * @return BufferedImage corresponding to provided Image.
	 */
	private static BufferedImage imageToBufferedImage(final Image image,
			int width, int height) {
		final BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = bufferedImage.createGraphics();
		g2.drawImage(image, 0, 0, width, height, null);
		g2.dispose();
		return bufferedImage;
	}

	/**
	 * Make provided image transparent wherever color matches the provided
	 * color.
	 * 
	 * @param im
	 *            BufferedImage whose color will be made transparent.
	 * @param color
	 *            Color in provided image which will be made transparent.
	 * @return Image with transparency applied.
	 */
	public static Image makeColorTransparent(final BufferedImage im,
			final Color color) {
		final ImageFilter filter = new RGBImageFilter() {
			// the color we are looking for (white)... Alpha bits are set to
			// opaque
			public int markerRGB = color.getRGB() | 0xFFFFFFFF;

			public final int filterRGB(final int x, final int y, final int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					// nothing to do
					return rgb;
				}
			}
		};

		final ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	public static boolean deletefile(File targetFolder, String fileName) {

		File fileImage = new File(targetFolder, fileName + ".png");
		// System.out.println(fileImage);

		return fileImage.delete();
	}

	public static BufferedImage resizeImageWithHint(
			BufferedImage originalImage, int type, Integer img_width,
			Integer img_height) {

		BufferedImage resizedImage = new BufferedImage(img_width, img_height,
				type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, img_width, img_height, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		return resizedImage;
	}
}
