package org.innovecs.services;

import java.util.List;
import java.util.Map;

import org.innovecs.models.Box;
import org.innovecs.models.BoxWrapper;

/**
 * @author spasko
 */
public interface FileService {

	List<Box> readBoxFile(String fileName);

	void writePositonsBoxFile(String string, Map<String, List<BoxWrapper>> result);

}
