package org.innovecs.services.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	private static final String TYPE = " type: ";
	private static final String END_XYZ = ")";
	private static final String X1_Y1_Z1_X2_Y2_Z2 = " ([x1, y1, z1, x2, y2, z2])=(";
	private static final String WEIGHT = " weight=";
	private static final String MULTIPLEXED = "(multiplexed)";
	private static final String BOX = "Box: ";
	private static final String PALLETA = "PALLETA: ";
	private static final String PALLETS = " pallets=";
	private static final String TOTAL_WEIGHT = " total weight= ";
	private static final String DESTINATION = "DESTINATION: ";
	public static String LINE_SEPARATOR = System.getProperty("line.separator");
	public static String TABULATOR = "--->";
	private static final Logger LOG = LogManager.getLogger(FileServiceImpl.class);

	@Override
	public List<Box> readBoxFile(String fileName) {
		validateFileType(fileName, "csv");
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

	private void validateFileType(String fileName, String type) {
		if (!fileName.endsWith(type)) {
			LOG.warn("Incorrect file {} type, must be {}", fileName, type);
		}
	}

	@Override
	public void writePositonsBoxFile(String filename, Map<String, List<BoxWrapper>> resultPackBoxes) {
		Path path = Paths.get(filename);
		StringBuilder sb = new StringBuilder();
		for (String destination : resultPackBoxes.keySet()) {
			List<BoxWrapper> pallets = resultPackBoxes.get(destination);
			int totalWeight = pallets.stream().mapToInt(BoxWrapper::getWeight).sum();
			sb.append(
					DESTINATION + destination + TOTAL_WEIGHT + totalWeight + PALLETS + pallets.size() + LINE_SEPARATOR);
			for (BoxWrapper palleta : pallets) {
				sb.append(
						TABULATOR + PALLETA + palleta.getName() + TOTAL_WEIGHT + palleta.getWeight() + LINE_SEPARATOR);
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
			sb.append(linerCur + BOX + bw.getName());
			sb.append(bw.isVirtual() ? MULTIPLEXED : "");
			sb.append(TYPE + bw.getBoxType().name().replace("_BLOCK_LAST_LAYER", ""));
			sb.append(WEIGHT + bw.getWeight());
			sb.append(X1_Y1_Z1_X2_Y2_Z2 + Arrays.toString(bw.getXyz()) + END_XYZ);
			sb.append(LINE_SEPARATOR);
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

	@Override
	public Map<String, List<BoxWrapper>> readPskFile(String fileName) {
		validateFileType(fileName, "psk");
		Map<String, List<BoxWrapper>> boxs = new HashMap<>();
		try (Scanner sc = new Scanner(new File(fileName))) {
			List<BoxWrapper> pallets = new ArrayList<>();
			List<BoxWrapper> type1 = new ArrayList<>();
			List<BoxWrapper> type2 = new ArrayList<>();
			List<BoxWrapper> type3 = new ArrayList<>();
			String dest = null;
			while (sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				if (line.startsWith(DESTINATION)) {
					dest = getSubst(line, DESTINATION, TOTAL_WEIGHT);
					pallets = new ArrayList<>();
					boxs.put(dest, pallets);
				} else if (line.contains(PALLETA)) {
					type1 = new ArrayList<>();
					String name = getSubst(line, PALLETA, TOTAL_WEIGHT);
					int weight = Integer.parseInt(line.substring(line.indexOf(TOTAL_WEIGHT) + TOTAL_WEIGHT.length()));
					BoxWrapper palleta = new BoxWrapper(name, BoxType.PALETTE, weight, dest);
					palleta.setBoxsInternal(type1);
					pallets.add(palleta);
				} else if (line.startsWith(TABULATOR + TABULATOR + BOX)) {
					type2 = readBoxWrapper(type1, dest, line);
				} else if (line.startsWith(TABULATOR + TABULATOR + TABULATOR + BOX)) {
					type3 = readBoxWrapper(type2, dest, line);
				} else if (line.startsWith(TABULATOR + TABULATOR + TABULATOR + TABULATOR + BOX)) {
					readBoxWrapper(type3, dest, line);
				}
			}
		} catch (FileNotFoundException e) {
			LOG.error("Cannot find file {}", fileName);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Incorrect file {} structure ", fileName);
		}
		return boxs;
	}

	private List<BoxWrapper> readBoxWrapper(List<BoxWrapper> typeExternal, String dest, String line) {
		String name = getSubst(line, BOX, TYPE);
		BoxType boxTypeInternal = BoxType.valueOf(getSubst(line, TYPE, WEIGHT));
		boolean isVirtual = false;
		if (name.contains(MULTIPLEXED)) {
			isVirtual = true;
			name = name.replace((CharSequence) MULTIPLEXED, (CharSequence) "");
		}
		int weight = Integer.parseInt(getSubst(line, WEIGHT, X1_Y1_Z1_X2_Y2_Z2));
		BoxWrapper bw = new BoxWrapper(name, boxTypeInternal, weight, dest);
		bw.setVirtual(isVirtual);
		int[] xyz = new int[6];
		String[] x1y1z1x2y2z2 = getSubst(line, X1_Y1_Z1_X2_Y2_Z2 + "[", "]" + END_XYZ).split(", ");
		for (int i = 0; i < 6; i++) {
			xyz[i] = Integer.parseInt(x1y1z1x2y2z2[i]);
		}

		bw.setXyz(xyz);
		typeExternal.add(bw);
		return bw.getBoxsInternal();
	}

	private String getSubst(String line, String str1, String str2) {
		return line.substring(line.indexOf(str1) + str1.length(), line.lastIndexOf(str2));
	}

}
