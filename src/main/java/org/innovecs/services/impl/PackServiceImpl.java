package org.innovecs.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
	public void calculatePack(List<Box> boxs) {
		// rotate
		optimalPackStrategy.selectOptimalVectorForInternalBox(BoxType.PALETTE, BoxType.TYPE1);
		optimalPackStrategy.selectOptimalVectorForInternalBox(BoxType.TYPE1, BoxType.TYPE2);
		optimalPackStrategy.selectOptimalVectorForInternalBox(BoxType.TYPE2, BoxType.TYPE3);

		// multiplexing to TYPE1
		List<BoxWrapper> boxsWrappers = boxs.stream().map(BoxWrapper::new).collect(Collectors.toList());
		boxsWrappers.addAll(multiplexBoxToBiggestLevel(boxsWrappers, BoxType.TYPE3, BoxType.TYPE2));
		boxsWrappers.addAll(multiplexBoxToBiggestLevel(boxsWrappers, BoxType.TYPE2, BoxType.TYPE1));
		boxsWrappers = boxsWrappers.stream().filter(bw -> BoxType.TYPE1.equals(bw.getBoxType()))
				.collect(Collectors.toList());
		Collections.sort(boxsWrappers);
		LOG.debug("Before multiplexing was {} Type1, after {} Type1",
				boxs.stream().filter(bw -> BoxType.TYPE1.equals(bw.getBoxType())).count(), boxsWrappers.size());

		// calculate need pallets
		int totalBoxsWeight = boxsWrappers.stream().mapToInt(BoxWrapper::getWeight).sum();
		LOG.debug("Total weight: " + totalBoxsWeight);
		int palletsNeed = (int) Math.round(0.499 + Math.max((double) totalBoxsWeight / Constants.PALLETE_MAXHEIGHT,
				(double) boxsWrappers.size() / BoxType.PALETTE.getCapacity()));
		LOG.debug("Total palletes need: " + palletsNeed);

		//
		packToPallets(boxsWrappers, palletsNeed);
	}

	private void packToPallets(List<BoxWrapper> boxsWrappers, int palletsNeed) {
		BoxWrapper[] pallets = buildPallets(boxsWrappers, palletsNeed);

		int oneLayerType1Capacity = BoxType.PALETTE.getTotalPozitionOnLength()
				* BoxType.PALETTE.getTotalPozitionOnWidth();
		int totalType1Layers = boxsWrappers.size() / oneLayerType1Capacity;
		int unfullType1LayerBoxsCount = boxsWrappers.size() % oneLayerType1Capacity;
		if (unfullType1LayerBoxsCount == 0) {
			LOG.debug("Ideal myltiplexing TYPE1");
			int intransitiveLayers = totalType1Layers / palletsNeed;
			int currentZ = 0;
			for (int i = 0; i < intransitiveLayers; i++) {
				List<BoxWrapper> subListLayer = boxsWrappers.subList(i * oneLayerType1Capacity * palletsNeed,
						(i + 1) * oneLayerType1Capacity * palletsNeed);
				Collections.shuffle(subListLayer);
				for (int j = 0; j < palletsNeed; j++) {
					List<BoxWrapper> subListLayerPallete = boxsWrappers.subList(j * oneLayerType1Capacity,
							(j + 1) * oneLayerType1Capacity);
					calculateCoordinats(pallets[j], currentZ, subListLayerPallete);

					LOG.debug("Pallete: {} layer: {}", pallets[j].getName(), i + 1);
					subListLayerPallete.stream()
							.map(bw -> " x1:" + bw.getXyz()[0] + " y1:" + bw.getXyz()[1] + " z1:" + bw.getXyz()[2]
									+ " x2:" + bw.getXyz()[3] + " y2:" + bw.getXyz()[4] + " z2:" + bw.getXyz()[5])
							.forEach(LOG::debug);
				}
				currentZ += BoxType.TYPE1.getHeight();
			}
		} else {
			totalType1Layers++;

		}
		if (boxsWrappers.stream().skip(boxsWrappers.size() - unfullType1LayerBoxsCount)
				.anyMatch(bw -> !bw.isVirtual())) {
			LOG.debug("Use only Type1 Layers, last layer has real box type1 (cannot unboxing)");
		} else
		// TODO
		if (unfullType1LayerBoxsCount >= oneLayerType1Capacity / 2 + 1) {

			LOG.debug("Use only Type1 Layers, last layer has more then half box type1");
		}
	}

	private BoxWrapper[] buildPallets(List<BoxWrapper> boxsWrappers, int palletsNeed) {
		BoxWrapper[] pallets = new BoxWrapper[palletsNeed];
		for (int i = 0; i < palletsNeed; i++) {
			Box box = new Box("Pallete-" + (i + 1), BoxType.PALETTE, 0, boxsWrappers.get(0).getDestination());
			BoxWrapper bw = new BoxWrapper(box);
			bw.getXyz()[0] = 0;
			bw.getXyz()[1] = 0;
			bw.getXyz()[2] = 0;
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
