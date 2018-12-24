package org.innovecs.services.impl;

import static org.innovecs.config.Constants.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.innovecs.config.Constants;
import org.innovecs.models.Box;
import org.innovecs.models.BoxType;
import org.innovecs.models.BoxWrapper;
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
			int lineNumb = 0;
			while (sc.hasNextLine()) {
				lineNumb++;
				String line = sc.nextLine().trim();
				readBoxFromFileLine(line, lineNumb, boxs);
			}
		} catch (FileNotFoundException e) {
			LOG.error("Cannot find file {}", fileName);
		}
		return boxs;
	}

	@Override
	public void writePositonsBoxFile(String filename, Map<String, List<BoxWrapper>> resultPackBoxes) {
		Path path = Paths.get(filename);
		StringBuilder sb = new StringBuilder();
		for (String destination : resultPackBoxes.keySet()) {
			List<BoxWrapper> pallets = resultPackBoxes.get(destination);
			int totalWeight = pallets.stream().mapToInt(BoxWrapper::getWeight).sum();
			sb.append("DESTINATION: " + destination + " total weight= " + totalWeight + " pallets=" + pallets.size()
					+ LINE_SEPARATOR);
			for (BoxWrapper palleta : pallets) {
				sb.append(TABULATOR + "PALLETA: " + palleta.getName() + " total weight= " + palleta.getWeight()
						+ LINE_SEPARATOR);
				appendBoxInfo(sb, palleta, TABULATOR);
			}

		}
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			writer.write(sb.toString());
		} catch (IOException e) {
			LOG.error("Cannot write to file {}", filename);
			e.printStackTrace();
		}
	}

	private void appendBoxInfo(StringBuilder sb, BoxWrapper boxs, String linerCur) {
		linerCur += TABULATOR;
		for (BoxWrapper bw : boxs.getBoxsInternal()) {
			sb.append(linerCur + "Box: " + bw.getName() + (bw.isVirtual() ? "(multiplexed)" : "") + " weight="
					+ bw.getWeight() + " ([x1, y1, z1, x2, y2, z2])=(" + Arrays.toString(bw.getXyz()) + ")" + LINE_SEPARATOR);
			appendBoxInfo(sb, bw, linerCur);
		}
	}

	private void readBoxFromFileLine(String line, int lineNumb, ArrayList<Box> boxs) {
		String[] values = line.split(Constants.CSV_FILE_DELIMETR);
		if (values.length != 4) {
			LOG.warn("Incorrect line number {} in input file: {}", lineNumb, line);
			return;
		}
		int weight = 0;
		BoxType boxType;
		try {
			weight = Integer.parseInt(values[2]);
			String boxTypeName = values[1].toUpperCase();
			if (!boxTypeName.matches("^TYPE[123]")) {
				throw new IllegalArgumentException();
			}
			boxType = BoxType.valueOf(boxTypeName);
		} catch (NumberFormatException nfe) {
			LOG.warn("Incorrect weight in line: {}", line);
			return;
		} catch (IllegalArgumentException iae) {
			LOG.warn("Incorrect box type in line: {}", line);
			return;
		}
		boxs.add(new Box(values[0], boxType, weight, values[3]));
	}

}
