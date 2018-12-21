package org.innovecs.models;

import org.innovecs.config.Constants;

/**
 * @author spasko
 */
public enum BoxType {
	TYPE1(Constants.BOX_TYPE1_WIDTH, Constants.BOX_TYPE1_LENGTH, Constants.BOX_TYPE1_HEIGHT),
	TYPE2(Constants.BOX_TYPE2_WIDTH, Constants.BOX_TYPE2_LENGTH, Constants.BOX_TYPE2_HEIGHT),
	TYPE3(Constants.BOX_TYPE3_WIDTH, Constants.BOX_TYPE3_LENGTH, Constants.BOX_TYPE3_HEIGHT);
	
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