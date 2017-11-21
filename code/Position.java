import java.io.Serializable;

public class Position implements Serializable {

	private static final long serialVersionUID = 1L;

	private int x;
	private int y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			Position outer = (Position)obj;
			return x == outer.x && y == outer.y; 
		}
		else {
			return false;
		}
	}

	public boolean isSibling(Position position) {
		return (x+1 == position.x && y+1 == position.y)
				|| (x+1 == position.x && y == position.y)
				|| (x+1 == position.x && y-1 == position.y)
				|| (x == position.x && y+1 == position.y)
				|| (x == position.x && y-1 == position.y)
				|| (x-1 == position.x && y+1 == position.y)
				|| (x-1 == position.x && y == position.y)
				|| (x-1 == position.x && y-1 == position.y);
	}

	@Override
	public String toString() {
		return String.format("%s - %s", x, y);
	}
}