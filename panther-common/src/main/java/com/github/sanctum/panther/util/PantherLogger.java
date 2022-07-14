package com.github.sanctum.panther.util;

import com.github.sanctum.panther.recursive.Service;
import com.github.sanctum.panther.recursive.ServiceFactory;
import com.github.sanctum.panther.recursive.ServiceLoader;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

public final class PantherLogger implements Service {

	final Obligation obligation = () -> "To provide quick access to a global logger.";
	Logger logger = Logger.getLogger("Panther");

	@Override
	public @NotNull Obligation getObligation() {
		return obligation;
	}

	public @NotNull("A logger isn't currently registered!") Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public static @NotNull PantherLogger getInstance() {
		PantherLogger service = ServiceFactory.getInstance().getService(PantherLogger.class);
		if (service == null) {
			ServiceLoader<PantherLogger> loader = ServiceFactory.getInstance().newLoader(PantherLogger.class);
			loader.supply(new PantherLogger());
			return loader.load();
		}
		return service;
	}

}
