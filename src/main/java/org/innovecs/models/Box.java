package org.innovecs.models;

/**
 * @author spasko
 */
public class Box {
	private String name;
	private BoxType boxType;
	private int weight;
	private String destination;

	public Box(String name, BoxType boxType, int weight, String destination) {
		this.name = name;
		this.boxType = boxType;
		this.weight = weight;
		this.destination = destination;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getName() {
		return name;
	}

	public BoxType getBoxType() {
		return boxType;
	}

	public String getDestination() {
		return destination;
	}

	@Override
	public String toString() {
		return "Box [name=" + name + ", boxType=" + boxType + ", weight=" + weight + ", destination=" + destination
				+ "]";
	}

}