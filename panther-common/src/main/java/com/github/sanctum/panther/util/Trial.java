package com.github.sanctum.panther.util;

import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.panther.file.Node;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Node.Pointer("com.github.sanctum.Trial")
public class Trial implements JsonAdapter<Trial> {

	private boolean finished;
	private final String subject;
	private final long time;
	private String signature = SignatureCheck.SIG;

	@Deprecated
	public Trial() {
		this.subject = "";
		this.time = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
	}

	public Trial(long time, String subject) {
		this.time = System.currentTimeMillis() + time;
		this.subject = subject;
	}

	public Trial(String signature, String subject, long time, boolean finished) throws IllegalAccessException {
		this.time = time;
		this.subject = subject == null ? "N/A" : subject;
		this.finished = finished;
		if (finished) {
			this.signature = null;
		}
		if (!SignatureCheck.isValid(signature)) throw new IllegalAccessException("Trial of " + '"' + subject + '"' + " expired.");
	}

	public Date getExpiration() {
		return new Date(time);
	}

	public long getExpirationRaw() {
		return time;
	}

	public boolean isUp() {
		return finished || (finished = (System.currentTimeMillis() >= time));
	}

	@Override
	public JsonElement write(Trial trial) {
		JsonObject o = new JsonObject();
		if (trial.signature != null) {
			o.addProperty("signature", trial.signature);
		}
		o.addProperty("time", trial.time);
		o.addProperty("subject", trial.subject);
		o.addProperty("finished", trial.finished);
		return o;
	}

	@Override
	public Trial read(Map<String, Object> object) {
		String subject = object.get("subject").toString();
		long time = Long.parseLong(String.valueOf(object.get("time")));
		boolean finished = Boolean.parseBoolean(object.get("finished").toString());
		if (time > 0) {
			try {
				String s = null;
				String sub = null;
				if (object.containsKey("signature")) {
					s = object.get("signature").toString();
				}
				if (object.containsKey("subject")) {
					sub = object.get("subject").toString();
				}
				return new Trial(s, sub, time, finished);
			} catch (IllegalAccessException e) {
				PantherLogger.getInstance().getLogger().severe("Your trial of " + '"' + subject + '"' + " is expired.");
				return null;
			}
		} else return null;
	}

	@Override
	public Class<? extends Trial> getSerializationSignature() {
		return Trial.class;
	}
}
