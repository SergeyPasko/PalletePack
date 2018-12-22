package org.innovecs.services;

import java.util.List;

import org.innovecs.models.Box;

/**
 * @author spasko
 */
public interface PackService {
	void calculatePack(List<Box> boxs);
}
