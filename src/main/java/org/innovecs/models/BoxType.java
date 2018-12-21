package org.innovecs.models;

/**
 * @author spasko
 */
public enum BoxType {
	TYPE1(1, 1, 1);
	private final int width;
	private final int length;
	private final int height;

	private BoxType(int width, int length, int height) {
		this.width = width;
		this.length = length;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getLength() {
		return length;
	}

	public int getHeight() {
		return height;
	}

}