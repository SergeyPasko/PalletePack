package org.innovecs.models;

import static org.innovecs.config.Constants.*;

/**
 * @author spasko
 */
public enum BoxType {
	TYPE1(BOX_TYPE1_WIDTH, BOX_TYPE1_LENGTH, BOX_TYPE1_HEIGHT), 
	TYPE2(BOX_TYPE2_WIDTH, BOX_TYPE2_LENGTH, BOX_TYPE2_HEIGHT),
	TYPE3(BOX_TYPE3_WIDTH, BOX_TYPE3_LENGTH, BOX_TYPE3_HEIGHT), 
	PALETTE(PALLETE_WIDTH, PALLETE_LENGTH, PALLETE_MAXHEIGHT),
	//Mock types for universal logic
	LAST_LAYER(-1,-1,-1),
	TYPE2_BLOCK_LAST_LAYER(BOX_TYPE2_WIDTH, BOX_TYPE2_LENGTH, BOX_TYPE2_HEIGHT), 
	TYPE3_BLOCK_LAST_LAYER(BOX_TYPE3_WIDTH, BOX_TYPE3_LENGTH, BOX_TYPE3_HEIGHT);
		
	private int width;
	private int length;
	private int height;

	private int totalPozitionOnWidth;
	private int totalPozitionOnLength;
	private int totalPozitionOnHeight;

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

	public void setWidth(int width) {
		this.width = width;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getCapacity() {
		return totalPozitionOnHeight * totalPozitionOnLength * totalPozitionOnWidth;
	}

	public int getTotalPozitionOnWidth() {
		return totalPozitionOnWidth;
	}

	public void setTotalPozitionOnWidth(int totalPozitionOnWidth) {
		this.totalPozitionOnWidth = totalPozitionOnWidth;
	}

	public int getTotalPozitionOnLength() {
		return totalPozitionOnLength;
	}

	public void setTotalPozitionOnLength(int totalPozitionOnLength) {
		this.totalPozitionOnLength = totalPozitionOnLength;
	}

	public int getTotalPozitionOnHeight() {
		return totalPozitionOnHeight;
	}

	public void setTotalPozitionOnHeight(int totalPozitionOnHeight) {
		this.totalPozitionOnHeight = totalPozitionOnHeight;
	}

}