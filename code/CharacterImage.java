import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class CharacterImage implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Position> positions = new ArrayList<Position>();
	private Position first;
	private BufferedImage image;
	private int width, height;
	private int initWidth, initHeight;

	private double proximidade;

	private char character;

	private boolean isLineBreak;

	public char getCharacter() {
		return character;
	}

	public void setCharacter(char character) {
		this.character = character;
	}

	public CharacterImage(Position fist) {
		this.first = fist;
		this.initWidth = this.first.getX();
		this.initHeight = this.first.getY();
		this.add(fist);
	}

	public boolean contains(Position position) {
		return positions.contains(position);
	}

	public void add(Position position) {
		if (width < position.getX()+1) {
			width = position.getX()+1;
		}
		if (height < position.getY()+1) {
			height = position.getY()+1;
		}
		if (initHeight > position.getY()) {
			initHeight = position.getY();
		}
		if (initWidth > position.getX()) {
			initWidth = position.getX();
		}
		positions.add(position);
	}

	public void add(CharacterImage character) {
		positions.addAll(character.getPositions());
		if (character.initWidth < initWidth) {
			initWidth = character.initWidth;
			first = character.first;
		}
		if (character.initHeight < initHeight) {
			initHeight = character.initHeight;
		}
		if (character.width > width) {
			width = character.width;
		}
		if (character.height > height) {
			height = character.height;
		}
	}

	public void addPosition(int x, int y) {
		add(new Position(x, y));
	}

	public boolean isCompl(CharacterImage character) {
		int space1 = initHeight - character.height;
		int space2 = character.initHeight - height;
		return (positions.size()/2) > character.positions.size()
				&& character.initWidth >= initWidth && character.width <= width
				&& ((space1 >= 0 && space1 < getHeight()/2)
						|| (space2 >= 0 && space2 < getHeight()/2));
	}

	public boolean isSelf(CharacterImage character) {
		for (Position position: positions) {
			if (character.haveSibling(position)) {
				return true;
			}
		}
		return false;
	}

	public boolean haveSibling(Position olter) {
		for (Position position: positions) {
			if (position.isSibling(olter)) {
				return true;
			}
		}
		return false;
	}

	public boolean isValid(int x, int y) {
		int with = image.getWidth(), height = image.getHeight();
		return x >= 0 && y >= 0 && with > x && height > y;
	}

	public List<Position> getPositionScale() {
		List<Position> positionsScale = new ArrayList<Position>();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (image.getRGB(x, y) != Color.WHITE.getRGB()) {
					positionsScale.add(new Position(x, y));
				}
			}
		}
		return positionsScale;
	}

	public void analisarProximidade(CharacterImage outer) {
		proximidade = 0;
		if (outer.image == null) {
			outer.newImage();
		}
		newImageResize(outer.getWidth(), outer.getHeight());
		List<Position> outerPositions = outer.getPositionScale();
		List<Position> positionsScale = getPositionScale();
		for (Position pScale: positionsScale) {
			if (outerPositions.contains(pScale)) {
				proximidade++;
			}
		}

		proximidade = ((proximidade*2)/(positionsScale.size()+outerPositions.size()))*100;

		image = null;
	}

	public void analisarProximidade2(CharacterImage outer) {
		proximidade = 0;
		newImage();
		outer.newImageResize(getWidth(), getHeight());
		List<Position> outerPositions = outer.getPositionScale();
		List<Position> positionsScale = getPositionScale();
		for (Position pScale: positionsScale) {
			if (outerPositions.contains(pScale)) {
				proximidade++;
			}
		}

		proximidade = ((proximidade*2)/(positionsScale.size()+outerPositions.size()))*100;

		image = null;
	}

	public BufferedImage newImageScale(int scale) {

		this.image = newImage();

		BufferedImage bi = new BufferedImage(scale * image.getWidth(null),
				scale * image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics2D grph = (Graphics2D) bi.getGraphics();
		grph.scale(scale, scale);

		grph.drawImage(image, 0, 0, null);
		grph.dispose();

		this.image = bi;
		return bi;
	}

	public  BufferedImage newImageResize(int width, int height) {
		this.image = newImage();
		int type=0;
		type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
		BufferedImage resizedImage = new BufferedImage(width, height,type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		this.image = resizedImage;
		return resizedImage;
	}

	public BufferedImage newImage() {
		this.image = createImage(width-initWidth, height-initHeight);
		for (Position position: positions) {
			image.setRGB(position.getX() - initWidth, position.getY() - initHeight, Color.BLACK.getRGB());
		}
		return this.image;
	}

	private BufferedImage createImage(int width, int height) {
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D g2d = (Graphics2D) newImage.getGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, width, height);
		g2d.dispose();

		return newImage;
	}

	public List<Position> getPositions() {
		return positions;
	}

	public int getWidth() {
		return width-initWidth;
	}

	public int getHeight() {
		return height-initHeight;
	}

	public double getProximidade() {
		return proximidade;
	}

	public int getInitHeight() {
		return initHeight;
	}

	public int getInitWidth() {
		return initWidth;
	}

	public double calculateSpaceWidth(CharacterImage characterImage) {
		return characterImage.getInitWidth()-width;
	}

	public double calculateSpaceHeight(CharacterImage characterImage) {
		return characterImage.getInitHeight()-height;
	}

	public int getCenterY() {
		return height-(getHeight()/2);
	}

	public int getCenterX() {
		return width-(getWidth()/2);
	}

	public boolean isLineBreak() {
		return isLineBreak;
	}

	public void setLineBreak(boolean isLineBreak) {
		this.isLineBreak = isLineBreak;
	}
}