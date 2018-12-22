package org.innovecs.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
		optimalPackStrategy.selectOptimalVectorForInternalBox(BoxType.PALETTE, BoxType.TYPE1);
		optimalPackStrategy.selectOptimalVectorForInternalBox(BoxType.TYPE1, BoxType.TYPE2);
		optimalPackStrategy.selectOptimalVectorForInternalBox(BoxType.TYPE2, BoxType.TYPE3);

		List<BoxWrapper> boxsWrappers = boxs.stream().map(BoxWrapper::new).collect(Collectors.toList());

		boxsWrappers.addAll(multiplexBoxToBiggestLevel(boxsWrappers, BoxType.TYPE3, BoxType.TYPE2));
		boxsWrappers.addAll(multiplexBoxToBiggestLevel(boxsWrappers, BoxType.TYPE2, BoxType.TYPE1));

		boxsWrappers = boxsWrappers.stream().filter(bw -> BoxType.TYPE1.equals(bw.getBoxType()))
				.collect(Collectors.toList());
		LOG.debug("Before multiplexing was {} Type1, after {} Type1",
				boxs.stream().filter(bw -> BoxType.TYPE1.equals(bw.getBoxType())).count(), boxsWrappers.size());
	}

	private List<BoxWrapper> multiplexBoxToBiggestLevel(List<BoxWrapper> boxs, BoxType fromBoxType, BoxType toBoxType) {
		List<BoxWrapper> boxsForMultiplex = boxs.stream().filter(box -> fromBoxType.equals(box.getBoxType()))
				.collect(Collectors.toList());
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
