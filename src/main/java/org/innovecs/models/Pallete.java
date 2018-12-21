package org.innovecs.models;

/**
 * @author spasko
 */
public class Pallete {

	private String name;
	private int width;
	private int length;
	private int maxHeight;
	private int maxWeight;
	private String destination;

	public Pallete(String name, int width, int length, int maxHeight, int maxWeight, String destination) {
		this.name = name;
		this.width = width;
		this.length = length;
		this.maxHeight = maxHeight;
		this.maxWeight = maxWeight;
		this.destination = destination;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public int getWidth() {
		return width;
	}

	public int getLength() {
		return length;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public int getMaxWeight() {
		return maxWeight;
	}

}
