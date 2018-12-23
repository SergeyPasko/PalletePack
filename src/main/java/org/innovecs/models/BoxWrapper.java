package org.innovecs.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author spasko
 */
public class BoxWrapper extends Box implements Comparable<BoxWrapper> {

	private List<BoxWrapper> boxsInternal = new ArrayList<>();
	private boolean included;
	private boolean virtual;
	private int[] xyz = new int[6];

	public BoxWrapper(String name, BoxType boxType, int weight, String destination) {
		super(name, boxType, weight, destination);
	}

	public BoxWrapper(Box box) {
		super(box.getName(), box.getBoxType(), box.getWeight(), box.getDestination());
	}

	public List<BoxWrapper> getBoxsInternal() {
		return boxsInternal;
	}

	public void setBoxsInternal(List<BoxWrapper> boxsInternal) {
		this.boxsInternal = boxsInternal;
	}

	public boolean isIncluded() {
		return included;
	}

	public void setIncluded(boolean included) {
		this.included = included;
	}

	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	@Override
	public String toString() {
		return "BoxWrapper [boxsInternal=" + boxsInternal + ", included=" + included + ", virtual=" + virtual + ", xyz="
				+ Arrays.toString(xyz) + ", toString()=" + super.toString() + "]";
	}

	public int[] getXyz() {
		return xyz;
	}

	public void setXyz(int[] xyz) {
		this.xyz = xyz;
	}

	@Override
	public int compareTo(BoxWrapper o) {
		return Comparator.comparing(BoxWrapper::isVirtual)
				.thenComparing(Comparator.comparing(BoxWrapper::getWeight).reversed()).compare(this, o);
	}

}
