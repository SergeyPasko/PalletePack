package org.innovecs.models;

import java.util.ArrayList;
import java.util.List;

/**
 * @author spasko
 */
public class Pallete {

	private String name;
	private String destination;
	private List<BoxWrapper> boxWrappers = new ArrayList<>();

	public Pallete(String name, String destination) {
		this.name = name;
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

	public List<BoxWrapper> getBoxWrappers() {
		return boxWrappers;
	}

	public void setBoxWrappers(List<BoxWrapper> boxWrappers) {
		this.boxWrappers = boxWrappers;
	}

}
