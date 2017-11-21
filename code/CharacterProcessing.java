import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;


public class CharacterProcessing {

	private static BufferedImage grayscale, binarized;

	private double averageSpacingWidth;
	private double averageSpacingHeight;

	public final static int WHITE = Color.WHITE.getRGB(), BLACK = Color.BLACK.getRGB();

	public List<CharacterImage> discover(BufferedImage image) throws IOException {
		image = processing(image);

		List<CharacterImage> characters = new ArrayList<CharacterImage>();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (WHITE != image.getRGB(x, y)) {
					Position position = new Position(x, y);
					CharacterImage fist = null;
					List<CharacterImage> remove = new ArrayList<CharacterImage>();
					for (CharacterImage character: characters) {
						if (character.haveSibling(position)) {
							if (fist == null) {
								character.add(position);
								fist = character;
							}
							else {
								fist.add(character);
								remove.add(character);
							}
							//break;
						}
					}
					characters.removeAll(remove);
					if (fist == null) {
						characters.add(new CharacterImage(position));
					}
				}
			}
		}

		List<Integer> heightLines = new ArrayList<>();
		boolean init = false;
		for (int y = 0; y < image.getHeight(); y++) {
			boolean isLine = true;
			for (int x = 0; x < image.getWidth(); x++) {
				if (WHITE != image.getRGB(x, y)) {
					init = true;
					isLine = false;
					break;
				}
			}
			if (init && isLine) {
				init = false;
				heightLines.add(y);
			}
		}

		List<CharacterImage> charactersByLine = new ArrayList<>();
		CharacterImage last = null;
		Integer topHeightLine = null;
		for (Integer heightLine: heightLines) {
			for (CharacterImage character: characters) {
				if (character.getInitHeight()+character.getHeight() <= heightLine
						&& (topHeightLine == null || character.getInitHeight() > topHeightLine)) {
					charactersByLine.add(character);
					last = character;
				}
			}
			last.setLineBreak(true);
			topHeightLine = heightLine;
		}
		last.setLineBreak(false);
		characters = charactersByLine;

		List<CharacterImage> remove = new ArrayList<CharacterImage>();
		for (CharacterImage character: characters) {
			for (CharacterImage characterOther: characters) {
				if (!character.equals(characterOther) && !remove.contains(characterOther)) {
					if (character.isCompl(characterOther)) {
						character.add(characterOther);
						remove.add(characterOther);
					}
					else if (characterOther.isCompl(character)) {
						characterOther.add(character);
						remove.add(character);
					}
				}	
			}
		}
		characters.removeAll(remove);

		CharacterImage previus = null;
		double sumSpacesWidth = 0, amountSpacesWidth = 0;
		double sumSpacesHeight = 0, amountSpacesHeight = 0;

		for (CharacterImage character: characters) {
			if (previus != null) {
				double spaceWidh = previus.calculateSpaceWidth(character);
				if (spaceWidh > 0) {
					sumSpacesWidth+=spaceWidh;
					amountSpacesWidth++;
				}

				double spaceHeight = previus.calculateSpaceHeight(character);
				if (spaceHeight > 0) {
					sumSpacesHeight+=spaceHeight;
					amountSpacesHeight++;	
				}
			}
			previus = character;
		}
		if (sumSpacesWidth > 0 && amountSpacesWidth > 0) {
			averageSpacingWidth = sumSpacesWidth / amountSpacesWidth;
		}
		if (sumSpacesHeight > 0 && amountSpacesHeight > 0) {
			averageSpacingHeight = sumSpacesHeight / amountSpacesHeight;
		}

		return characters;
	}

	public static BufferedImage processing(BufferedImage image) throws IOException {
		grayscale = toGray(image);
		binarized = binarize(grayscale);
		//writeImage("output_f");   
		return binarized;
	}

	public static BufferedImage processing2(BufferedImage image) throws IOException {
		return toBinary(toGrayscale(image), 215);
	}

	private static BufferedImage toGrayscale(BufferedImage image)  
			throws IOException {  
		BufferedImage output = new BufferedImage(image.getWidth(),  
				image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);  
		Graphics2D g2d = output.createGraphics();  
		g2d.drawImage(image, 0, 0, null);  
		g2d.dispose();  
		return output;  
	} 

	private static BufferedImage toBinary(BufferedImage image, int t) {  
		int BLACK = Color.BLACK.getRGB();  
		int WHITE = Color.WHITE.getRGB();  

		BufferedImage output = new BufferedImage(image.getWidth(),  
				image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);  

		for (int y = 0; y < image.getHeight(); y++)  
			for (int x = 0; x < image.getWidth(); x++) {  
				Color pixel = new Color(image.getRGB(x, y));  
				output.setRGB(x, y, pixel.getRed() < t ? BLACK : WHITE);  
			}  

		return output;  
	}

	private static void writeImage(String output) throws IOException {
		File file = new File("test-resource/result-processing/"+output+".png");
		ImageIO.write(binarized, "jpg", file);
	}

	public static int[] imageHistogram(BufferedImage input) {
		int[] histogram = new int[256];

		for(int i=0; i<histogram.length; i++) histogram[i] = 0;

		for(int i=0; i<input.getWidth(); i++) {
			for(int j=0; j<input.getHeight(); j++) {
				int red = new Color(input.getRGB (i, j)).getRed();
				histogram[red]++;
			}
		}

		return histogram;
	}

	private static BufferedImage toGray(BufferedImage original) {
		int alpha, red, green, blue;
		int newPixel;

		BufferedImage lum = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

		for(int i=0; i<original.getWidth(); i++) {
			for(int j=0; j<original.getHeight(); j++) {

				alpha = new Color(original.getRGB(i, j)).getAlpha();
				red = new Color(original.getRGB(i, j)).getRed();
				green = new Color(original.getRGB(i, j)).getGreen();
				blue = new Color(original.getRGB(i, j)).getBlue();

				red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
				newPixel = colorToRGB(alpha, red, red, red);

				lum.setRGB(i, j, newPixel);

			}
		}

		return lum;
	}

	private static int otsuTreshold(BufferedImage original) {
		int[] histogram = imageHistogram(original);
		int total = original.getHeight() * original.getWidth();

		float sum = 0;
		for(int i=0; i<256; i++) sum += i * histogram[i];

		float sumB = 0;
		int wB = 0;
		int wF = 0;

		float varMax = 0;
		int threshold = 0;

		for(int i=0 ; i<256 ; i++) {
			wB += histogram[i];
			if(wB == 0) continue;
			wF = total - wB;

			if(wF == 0) break;

			sumB += (float) (i * histogram[i]);
			float mB = sumB / wB;
			float mF = (sum - sumB) / wF;

			float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

			if(varBetween > varMax) {
				varMax = varBetween;
				threshold = i;
			}
		}

		return threshold;
	}

	private static BufferedImage binarize(BufferedImage original) {
		int red;
		int newPixel;

		int threshold = otsuTreshold(original);

		BufferedImage binarized = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

		for(int i=0; i<original.getWidth(); i++) {
			for(int j=0; j<original.getHeight(); j++) {

				red = new Color(original.getRGB(i, j)).getRed();
				int alpha = new Color(original.getRGB(i, j)).getAlpha();
				if(red > threshold) {
					newPixel = 255;
				}
				else {
					newPixel = 0;
				}
				newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);
				binarized.setRGB(i, j, newPixel); 

			}
		}
		return binarized;
	}

	private static int colorToRGB(int alpha, int red, int green, int blue) {
		int newPixel = 0;
		newPixel += alpha;
		newPixel = newPixel << 8;
		newPixel += red; newPixel = newPixel << 8;
		newPixel += green; newPixel = newPixel << 8;
		newPixel += blue;

		return newPixel;
	}

	public boolean isBlankSpace(CharacterImage previus, CharacterImage next) {
		boolean blankSpace = false;
		if (previus != null && next != null) {
			double space = previus.calculateSpaceWidth(next);
			blankSpace = space > averageSpacingWidth*1.8;
		}
		return blankSpace;
	}
}