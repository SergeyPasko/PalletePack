package org.innovecs.services.impl;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.innovecs.models.BoxType;
import org.innovecs.services.OptimalPackStrategy;
import org.springframework.stereotype.Service;

/**
 * @author spasko
 */
@Service
public class OptimalPackStrategyImpl implements OptimalPackStrategy {
	private static final Logger LOG = LogManager.getLogger(OptimalPackStrategyImpl.class);

	// Only symmetric solutions
	@Override
	public void selectOptimalVectorForInternalBox(BoxType boxExternal, BoxType boxInternal) {
		int maxAt = findVector(boxExternal, boxInternal);
		rotateOperation(boxInternal, maxAt);
		calculateTotalPozitions(boxExternal, boxInternal);
	}

	private void calculateTotalPozitions(BoxType boxExternal, BoxType boxInternal) {
		boxExternal.setTotalPozitionOnLength(boxExternal.getLength() / boxInternal.getLength());
		boxExternal.setTotalPozitionOnWidth(boxExternal.getWidth() / boxInternal.getWidth());
		boxExternal.setTotalPozitionOnHeight(boxExternal.getHeight() / boxInternal.getHeight());
		LOG.debug("Box {} can iclude {} count of {}", boxExternal, boxExternal.getCapacity(), boxInternal);
	}

	private int findVector(BoxType boxExternal, BoxType boxInternal) {
		int[] variants = new int[6];

		variants[0] = (boxExternal.getLength() / boxInternal.getLength())
				* (boxExternal.getWidth() / boxInternal.getWidth())
				* (boxExternal.getHeight() / boxInternal.getHeight());
		variants[1] = (boxExternal.getLength() / boxInternal.getWidth())
				* (boxExternal.getWidth() / boxInternal.getLength())
				* (boxExternal.getHeight() / boxInternal.getHeight());

		variants[2] = (boxExternal.getLength() / boxInternal.getHeight())
				* (boxExternal.getWidth() / boxInternal.getWidth())
				* (boxExternal.getHeight() / boxInternal.getLength());
		variants[3] = (boxExternal.getLength() / boxInternal.getHeight())
				* (boxExternal.getWidth() / boxInternal.getLength())
				* (boxExternal.getHeight() / boxInternal.getWidth());

		variants[4] = (boxExternal.getLength() / boxInternal.getWidth())
				* (boxExternal.getWidth() / boxInternal.getHeight())
				* (boxExternal.getHeight() / boxInternal.getLength());
		variants[5] = (boxExternal.getLength() / boxInternal.getLength())
				* (boxExternal.getWidth() / boxInternal.getHeight())
				* (boxExternal.getHeight() / boxInternal.getWidth());

		LOG.debug("Variants in different rotations {}", Arrays.toString(variants));

		int maxAt = 0;
		for (int i = 0; i < variants.length; i++) {
			maxAt = variants[i] > variants[maxAt] ? i : maxAt;
		}
		return maxAt;
	}

	private void rotateOperation(BoxType boxInternal, int maxAt) {
		int temp;
		switch (maxAt) {
		case 0:
			LOG.debug("Not rotation");
			break;
		case 1:
			LOG.debug("Rotate over height");
			temp = boxInternal.getLength();
			boxInternal.setLength(boxInternal.getWidth());
			boxInternal.setWidth(temp);
			break;
		case 2:
			LOG.debug("Rotate over width");
			temp = boxInternal.getHeight();
			boxInternal.setHeight(boxInternal.getLength());
			boxInternal.setLength(temp);
			break;
		case 5:
			LOG.debug("Rotate over length");
			temp = boxInternal.getWidth();
			boxInternal.setWidth(boxInternal.getHeight());
			boxInternal.setHeight(temp);
			break;
		case 3:
			LOG.debug("Rotate over width and lenth");
			temp = boxInternal.getHeight();
			boxInternal.setHeight(boxInternal.getWidth());
			boxInternal.setWidth(boxInternal.getLength());
			boxInternal.setLength(temp);
			break;
		case 4:
			LOG.debug("Rotate over height and length");
			temp = boxInternal.getHeight();
			boxInternal.setHeight(boxInternal.getLength());
			boxInternal.setLength(boxInternal.getWidth());
			boxInternal.setWidth(temp);
			break;

		default:
			LOG.error("Mistake in logic for rotation box");
		}
	}

	@PostConstruct
	private void init() {
		// rotate models to grid packaging
		selectOptimalVectorForInternalBox(BoxType.PALETTE, BoxType.TYPE1);
		selectOptimalVectorForInternalBox(BoxType.TYPE1, BoxType.TYPE2);
		selectOptimalVectorForInternalBox(BoxType.TYPE2, BoxType.TYPE3);
	}
}
