package com.github.sanctum.panther.util;

import java.io.Serializable;

/**
 * Use this functional interface to form lambdas/references that execute on run time for you.
 * Either passing values or running code, this can come in handy and also extends functionality from the Serializable interface.
 *
 * @author Hempfest
 */
@FunctionalInterface
public interface Applicable extends Runnable, Serializable {

	@Override
	void run();

	/**
	 * Execute any information applied within reference.
	 */
	default Applicable applyBefore(Applicable applicable) {
		applicable.run();
		return this;
	}
}