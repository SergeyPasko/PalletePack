package org.innovecs.services;

import java.util.List;

import org.innovecs.models.Box;
import org.innovecs.models.BoxWrapper;

/**
 * @author spasko
 */
public interface PackService {
	List<BoxWrapper> calculatePack(List<Box> boxs);
}
