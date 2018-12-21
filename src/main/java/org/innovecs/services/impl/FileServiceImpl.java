package org.innovecs.services.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.innovecs.config.Constants;
import org.innovecs.models.Box;
import org.innovecs.models.BoxType;
import org.innovecs.services.FileService;
import org.springframework.stereotype.Service;

/**
 * @author spasko
 */
@Service
public class FileServiceImpl implements FileService {
	private static final Logger LOG = LogManager.getLogger(FileServiceImpl.class);

	@Override
	public List<Box> readBoxFile(String fileName) {
		ArrayList<Box> boxs = new ArrayList<>();
		try (Scanner sc = new Scanner(new File(fileName))) {
			while (sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				readBoxFromFileLine(line, boxs);
			}
		} catch (FileNotFoundException e) {
			LOG.error("Cannot find file {}", fileName);
		}
		return boxs;
	}

	@Override
	public void writePositonsBoxFile() {
		// TODO Auto-generated method stub

	}

	private void readBoxFromFileLine(String line, ArrayList<Box> boxs) {
		String[] values = line.split(Constants.CSV_FILE_DELIMETR);
		if (values.length != 4) {
			LOG.warn("Incorrect line in input file {}", line);
			return;
		}
		int weight = 0;
		BoxType boxType;
		try {
			weight = Integer.parseInt(values[2]);
			boxType = BoxType.valueOf(values[1].toUpperCase());
		} catch (NumberFormatException nfe) {
			LOG.warn("Incorrect weight in line {}", line);
			return;
		} catch (IllegalArgumentException iae) {
			LOG.warn("Incorrect box type in line {}", line);
			return;
		}
		boxs.add(new Box(values[0], boxType, weight, values[3]));
	}

}