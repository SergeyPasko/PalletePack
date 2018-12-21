package org.innovecs.services;

import java.util.List;

import org.innovecs.models.Box;

/**
 * @author spasko
 */
public interface FileService {

	List<Box> readBoxFile(String fileName);

	void writePositonsBoxFile();

}
