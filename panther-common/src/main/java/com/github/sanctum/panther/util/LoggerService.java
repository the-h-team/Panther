package com.github.sanctum.panther.util;

import com.github.sanctum.panther.recursive.Service;
import com.github.sanctum.panther.recursive.ServiceFactory;
import com.github.sanctum.panther.recursive.ServiceLoader;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

public final class LoggerService implements Service {

	final Obligation obligation = () -> "To provide quick access to a global logger.";
	Logger logger;

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

	public static @NotNull LoggerService getInstance() {
		LoggerService service = ServiceFactory.getInstance().getService(LoggerService.class);
		if (service == null) {
			ServiceLoader<LoggerService> loader = ServiceFactory.getInstance().newLoader(LoggerService.class);
			loader.supply(new LoggerService());
			return loader.load();
		}
		return service;
	}

}
