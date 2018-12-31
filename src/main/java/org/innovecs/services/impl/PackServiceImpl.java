package org.innovecs.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.innovecs.config.Constants;
import org.innovecs.models.Box;
import org.innovecs.models.BoxType;
import org.innovecs.models.BoxWrapper;
import org.innovecs.services.OptimalPackStrategy;
import org.innovecs.services.PackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author spasko
 */
@Service
public class PackServiceImpl implements PackService {
	private static final Logger LOG = LogManager.getLogger(PackServiceImpl.class);

	@Autowired
	private OptimalPackStrategy optimalPackStrategy;

	@Override
	public List<BoxWrapper> calculatePack(List<Box> boxs) {

		// multiplexing to TYPE1
		List<BoxWrapper> boxsWrappers = multiplexBoxsToType1(boxs);

		// calculate need pallets count
		int palletsNeed = totalPalletsNeed(boxsWrappers);

		return Arrays.asList(packToPallets(boxsWrappers, palletsNeed));
	}

	@Override
	public Map<String, List<BoxWrapper>> allPack(List<Box> boxs) {
		Map<String, List<org.innovecs.models.Box>> boxesByDestinations = boxs.stream()
				.collect(Collectors.groupingBy(org.innovecs.models.Box::getDestination));

		return boxesByDestinations.values().stream().flatMap(b -> calculatePack(b).stream())
				.collect(Collectors.groupingBy(BoxWrapper::getDestination));
	}

	private List<BoxWrapper> multiplexBoxsToType1(List<Box> boxs) {
		List<BoxWrapper> boxsWrappers = boxs.stream().map(BoxWrapper::new).collect(Collectors.toList());
		boxsWrappers.addAll(multiplexBoxToBiggestLevel(boxsWrappers, BoxType.TYPE3, BoxType.TYPE2));
		boxsWrappers.addAll(multiplexBoxToBiggestLevel(boxsWrappers, BoxType.TYPE2, BoxType.TYPE1));
		boxsWrappers = boxsWrappers.stream().filter(bw -> BoxType.TYPE1.equals(bw.getBoxType()))
				.collect(Collectors.toList());
		Collections.sort(boxsWrappers);
		LOG.debug("Before multiplexing was {} Type1, after {} Type1",
				boxs.stream().filter(bw -> BoxType.TYPE1.equals(bw.getBoxType())).count(), boxsWrappers.size());
		return boxsWrappers;
	}

	private int totalPalletsNeed(List<BoxWrapper> boxsWrappers) {
		int totalBoxsWeight = boxsWrappers.stream().mapToInt(BoxWrapper::getWeight).sum();
		LOG.debug("Total weight: " + totalBoxsWeight);
		int palletsNeed = (int) Math.round(0.499 + Math.max((double) totalBoxsWeight / Constants.PALLETE_MAXHEIGHT,
				(double) boxsWrappers.size() / BoxType.PALETTE.getCapacity()));
		LOG.debug("Total palletes need: " + palletsNeed);
		return palletsNeed;
	}

	private BoxWrapper[] packToPallets(List<BoxWrapper> boxsWrappers, int palletsNeed) {
		BoxWrapper[] pallets = buildPallets(boxsWrappers, palletsNeed);
		int oneLayerType1Capacity = BoxType.PALETTE.getTotalPozitionOnLength()
				* BoxType.PALETTE.getTotalPozitionOnWidth();

		int currentZ = 0;
		layers: for (int layer = 0;; layer++) {
			int startSubListIndex = layer * oneLayerType1Capacity * palletsNeed;
			int endSubListIndex = (layer + 1) * oneLayerType1Capacity * palletsNeed - 1;
			if (endSubListIndex < boxsWrappers.size()) {
				Collections.shuffle(boxsWrappers.subList(startSubListIndex, endSubListIndex));
			}
			for (int j = 0; j < palletsNeed; j++) {
				startSubListIndex = oneLayerType1Capacity * (layer * palletsNeed + j);
				if (startSubListIndex >= boxsWrappers.size()) {
					break layers;
				}
				endSubListIndex = oneLayerType1Capacity * (layer * palletsNeed + j + 1);
				if (endSubListIndex > boxsWrappers.size()) {
					endSubListIndex = boxsWrappers.size();
				}
				List<BoxWrapper> subListLayerPallete = boxsWrappers.subList(startSubListIndex, endSubListIndex);
				if (subListLayerPallete.size() < oneLayerType1Capacity
						&& subListLayerPallete.stream().allMatch(bw -> bw.isVirtual())) {
					LOG.debug("This is last layer, without real TYPE1 boxs");
					subListLayerPallete = demultiplexAndPack(currentZ, subListLayerPallete);
				} else {
					calculateCoordinats(pallets[j], currentZ, subListLayerPallete);
				}
				calcCoordinatsInternalBoxes(currentZ, subListLayerPallete);
				pallets[j].getBoxsInternal().addAll(subListLayerPallete);
				pallets[j].setWeight(
						pallets[j].getWeight() + subListLayerPallete.stream().mapToInt(BoxWrapper::getWeight).sum());

				LOG.debug("Pallete: {} layer: {}", pallets[j].getName(), layer + 1);
				subListLayerPallete.stream()
						.map(bw -> bw.getName() + " weight:" + bw.getWeight() + "---x1:" + bw.getXyz()[0] + " y1:"
								+ bw.getXyz()[1] + " z1:" + bw.getXyz()[2] + " x2:" + bw.getXyz()[3] + " y2:"
								+ bw.getXyz()[4] + " z2:" + bw.getXyz()[5])
						.forEach(LOG::debug);
			}
			currentZ += BoxType.TYPE1.getHeight();
		}

		return pallets;
	}

	private void calcCoordinatsInternalBoxes(int currentZ, List<BoxWrapper> subListLayerPallete) {
		for (BoxWrapper bw : subListLayerPallete) {
			if (!bw.getBoxsInternal().isEmpty()) {
				LOG.debug("Calculate coordinats of internal boxes for {} with names:{}", bw.getName(),
						bw.getBoxsInternal().stream().map(BoxWrapper::getName).collect(Collectors.joining(",")));
				calculateCoordinats(bw, currentZ, bw.getBoxsInternal());
				calcCoordinatsInternalBoxes(currentZ, bw.getBoxsInternal());
			}
		}
	}

	private List<BoxWrapper> demultiplexAndPack(int currentZ, List<BoxWrapper> boxs) {
		// Specific logic without changes basic box types
		BoxType type2llBoxType = BoxType.TYPE2_BLOCK_LAST_LAYER;
		BoxType type3llBoxType = BoxType.TYPE3_BLOCK_LAST_LAYER;
		BoxType externalBoxType = BoxType.LAST_LAYER;

		int heightExternalBox = IntStream
				.of(type2llBoxType.getHeight(), type2llBoxType.getWidth(), type2llBoxType.getLength()).min().getAsInt();
		externalBoxType.setHeight(heightExternalBox);
		externalBoxType.setLength(BoxType.PALETTE.getTotalPozitionOnLength() * BoxType.TYPE1.getLength());
		externalBoxType.setWidth(BoxType.PALETTE.getTotalPozitionOnWidth() * BoxType.TYPE1.getWidth());

		optimalPackStrategy.selectOptimalVectorForInternalBox(externalBoxType, type2llBoxType);
		if (externalBoxType.getCapacity() < boxs.size()) {
			externalBoxType.setHeight(37 * heightExternalBox);
			optimalPackStrategy.selectOptimalVectorForInternalBox(externalBoxType, type2llBoxType);
		}
		optimalPackStrategy.selectOptimalVectorForInternalBox(type2llBoxType, type3llBoxType);
		boxs = boxs.stream().flatMap(bw -> bw.getBoxsInternal().stream()).collect(Collectors.toList());
		boxs.forEach(bw -> {
			bw.setBoxType(type2llBoxType);
			bw.getBoxsInternal().forEach(ibw -> ibw.setBoxType(type3llBoxType));
		});

		BoxWrapper lastLayer = new BoxWrapper(new Box("lastLayer", externalBoxType, 0, boxs.get(0).getDestination()));
		lastLayer.setVirtual(true);
		lastLayer.getXyz()[0] = 0;
		lastLayer.getXyz()[1] = 0;
		lastLayer.getXyz()[2] = currentZ;
		calculateCoordinats(lastLayer, currentZ, boxs);

		return boxs;
	}

	private BoxWrapper[] buildPallets(List<BoxWrapper> boxsWrappers, int palletsNeed) {
		BoxWrapper[] pallets = new BoxWrapper[palletsNeed];
		for (int i = 0; i < palletsNeed; i++) {
			Box box = new Box("Pallete-" + (i + 1), BoxType.PALETTE, 0, boxsWrappers.get(0).getDestination());
			BoxWrapper bw = new BoxWrapper(box);
			bw.getXyz()[0] = 0;
			bw.getXyz()[1] = 0;
			bw.getXyz()[2] = 0;
			bw.setWeight(0);
			bw.setVirtual(true);
			pallets[i] = bw;
		}
		return pallets;
	}

	private void calculateCoordinats(BoxWrapper externalBox, int currentZofLayer, List<BoxWrapper> boxs) {
		int x1, x2, y1, y2, z1, z2;
		BoxType internalBoxType = boxs.get(0).getBoxType();
		BoxType externalBoxType = externalBox.getBoxType();

		if (BoxType.PALETTE.equals(externalBoxType)) {
			z1 = currentZofLayer;
		} else {
			z1 = externalBox.getXyz()[2];
		}

		int numEl = 0;
		totalloop: for (int h = 0; h < externalBoxType.getTotalPozitionOnHeight(); h++) {
			z2 = z1 + internalBoxType.getHeight();
			x1 = externalBox.getXyz()[0];
			for (int l = 0; l < externalBoxType.getTotalPozitionOnLength(); l++) {
				x2 = x1 + internalBoxType.getLength();
				y1 = externalBox.getXyz()[1];
				for (int w = 0; w < externalBoxType.getTotalPozitionOnWidth(); w++) {
					y2 = y1 + internalBoxType.getWidth();
					int[] xyz = boxs.get(numEl).getXyz();
					xyz[0] = x1;
					xyz[1] = y1;
					xyz[2] = z1;
					xyz[3] = x2;
					xyz[4] = y2;
					xyz[5] = z2;
					numEl++;
					if (numEl == boxs.size()) {
						break totalloop;
					}
					y1 += internalBoxType.getWidth();
				}
				x1 += internalBoxType.getLength();
			}
			z1 += internalBoxType.getHeight();
		}

	}

	private List<BoxWrapper> multiplexBoxToBiggestLevel(List<BoxWrapper> boxs, BoxType fromBoxType, BoxType toBoxType) {
		List<BoxWrapper> boxsForMultiplex = boxs.stream().filter(box -> fromBoxType.equals(box.getBoxType()))
				.collect(Collectors.toList());
		Collections.shuffle(boxsForMultiplex);
		List<BoxWrapper> boxsExternal = new ArrayList<>();
		int curCapacity = 0;
		BoxWrapper boxExternal = null;
		for (BoxWrapper boxForMultiplex : boxsForMultiplex) {
			if (curCapacity % toBoxType.getCapacity() == 0) {
				boxExternal = new BoxWrapper(toBoxType + "_" + curCapacity, toBoxType, 0,
						boxForMultiplex.getDestination());
				boxExternal.setVirtual(true);
				boxsExternal.add(boxExternal);
				LOG.debug("Create virtual box {}", boxExternal.getName());
			}
			curCapacity++;
			boxExternal.setWeight(boxExternal.getWeight() + boxForMultiplex.getWeight());
			boxForMultiplex.setIncluded(true);
			boxExternal.getBoxsInternal().add(boxForMultiplex);
			LOG.debug("Include box {} to virtual box {}", boxForMultiplex.getName(), boxExternal.getName());
		}
		return boxsExternal;
	}

}
