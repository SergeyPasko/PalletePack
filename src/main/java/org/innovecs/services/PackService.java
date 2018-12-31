package org.innovecs.services;

import java.util.List;
import java.util.Map;

import org.innovecs.models.Box;
import org.innovecs.models.BoxWrapper;

/**
 * @author spasko
 */
public interface PackService {
	List<BoxWrapper> calculatePack(List<Box> boxs);

	Map<String, List<BoxWrapper>> allPack(List<Box> boxs);
}
