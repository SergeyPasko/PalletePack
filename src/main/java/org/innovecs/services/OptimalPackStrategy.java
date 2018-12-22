package org.innovecs.services;

import org.innovecs.models.BoxType;

/**
 * @author spasko
 */
public interface OptimalPackStrategy {

	void selectOptimalVectorForInternalBox(BoxType boxExternal, BoxType boxInternal);

}
