package com.github.sanctum.panther.file;

import com.github.sanctum.panther.util.EasyTypeAdapter;
import com.github.sanctum.panther.util.LoggerService;
import com.github.sanctum.panther.util.MapDecompressionService;
import com.github.sanctum.panther.util.SimpleAsynchronousTask;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author Hempfest
 * @version 1.0
 */
public class JsonConfiguration extends Configurable {

	private final File file;
	private final File parent;
	private final String name;
	private final String directory;
	// We use json simple because they inherit java collection types for easy casting.
	protected JSONObject json;
	private final JSONParser parser;

	public JsonConfiguration(@NotNull File folder, @NotNull String name, @Nullable String directory) {
		this.parser = new JSONParser();
		this.name = name;
		this.directory = directory;
		if (!folder.exists()) {
			//noinspection ResultOfMethodCallIgnored
			folder.mkdir();
		}

		final File parent = (directory == null || directory.isEmpty()) ? folder : new File(folder, directory);
		if (!parent.exists()) {
			//noinspection ResultOfMethodCallIgnored
			parent.mkdir();
		}
		this.parent = parent;
		this.file = new File(parent, name.concat(".json"));
		File toRemove = new File(parent, name.concat(".data"));
		if (toRemove.exists()) {
			try {
				load(toRemove);
			} catch (Exception ex) {
				json = new JSONObject();
			}
			if (toRemove.delete()) {
				save();
			}
		} else {
			try {
				load(file);
			} catch (Exception ex) {
				json = new JSONObject();
			}
		}
	}

	public boolean load(@NotNull File file) throws Exception {
		if (file.exists()) {
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
			json = (JSONObject) parser.parse(reader);
			reader.close();
			fileInputStream.close();
			return true;
		} else {
			json = new JSONObject();
		}
		return false;
	}

	@Override
	public void reload() {
		try {
			if (!file.exists()) {
				PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8.toString());
				writer.print("{");
				writer.print("}");
				writer.flush();
				writer.close();
			}
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
			json = (JSONObject) parser.parse(reader);
			reader.close();
			fileInputStream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean save() {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			Gson g = JsonAdapter.getJsonBuilder().setPrettyPrinting().disableHtmlEscaping().enableComplexMapKeySerialization().serializeNulls().serializeSpecialFloatingPointValues().create();
			g.toJson(json, Map.class, writer);
			writer.flush();
			writer.close();
			return true;
		} catch (Exception ex) {
			LoggerService.getInstance().getLogger().severe("- An object of unknown origin was attempted to be saved and failed.");
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean delete() {
		memory.clear();
		return file.delete();
	}

	@Override
	public boolean create() throws IOException {
		if (parent.exists()) {
			if (!file.exists()) {
				reload();
				return true;
			} else {
				return false;
			}
		}
		return parent.mkdirs() && file.createNewFile();
	}

	@Override
	public boolean exists() {
		return parent.exists() && file.exists();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDirectory() {
		return this.directory;
	}

	@Override
	public File getParent() {
		return file;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void set(String key, Object o) {
		String[] a = key.split("\\.");
		String k = a[Math.max(0, a.length - 1)];
		JSONObject ob = json;
		for (int i = 0; i < a.length - 1; i++) {
			String pathKey = a[i];
			Object os = ob.get(pathKey);
			if (os instanceof JSONObject) {
				ob = (JSONObject) os;
			} else {
				JSONObject n = new JSONObject();
				ob.put(pathKey, n);
				ob = (JSONObject) ob.get(pathKey);
			}
		}
		if (o == null) {
			ob.remove(k);
			return;
		}
		if (o instanceof Map) {
			ob.put(k, new JSONObject((Map<?, ?>) o));
			return;
		}
		if (o instanceof Collection) {
			JSONArray ar = new JSONArray();
			ar.addAll((List<?>) o);
			ob.put(k, ar);
			return;
		}
		ob.put(k, o);
	}

	@SuppressWarnings("unchecked")
	Object checkObject(java.lang.reflect.Type type, boolean array, Object object) {
		Object target = object;
		try {
			Class<?> cl = Class.forName(type.getTypeName());
			//if (type == ItemStack.class) type = JsonItemStack.class;
			if (target instanceof JSONObject) {
				JSONObject j = (JSONObject) object;
				Gson g = JsonAdapter.getJsonBuilder().create();

				Map.Entry<String, JsonAdapterInput<?>> d = serializers.entrySet().stream().filter(de -> de.getKey().equals(cl.getTypeName()) || cl.isAssignableFrom(de.getValue().getSerializationSignature())).findFirst().orElse(null);
				if (d != null) {
					if (j.containsKey(d.getKey())) {
						Object ob = j.get(d.getKey());
						Object o;
						if (ob instanceof String) {
							Map<String, Object> map = g.fromJson((String) ob, new EasyTypeAdapter<Map<String, Object>>());
							o = d.getValue().read(map);
						} else {
							o = d.getValue().read((Map<String, Object>) ob);
						}
						if (o != null) {
							target = o;
						}
					}
				}
				return target;
			}
			if (target instanceof JSONArray && array) {
				JSONArray j = (JSONArray) object;
				Map.Entry<String, JsonAdapterInput<?>> d = serializers.entrySet().stream().filter(de -> cl.isAssignableFrom(de.getValue().getSerializationSignature())).findFirst().orElse(null);
				if (d != null) {
					Object[] copy = (Object[]) Array.newInstance(cl, j.size());
					for (int i = 0; i < j.size(); i++) {
						Map<String, Object> map = (Map<String, Object>) j.get(i);
						copy[i] = d.getValue().read(map.containsKey(d.getKey()) ? (Map<String, Object>) map.get(d.getKey()) : map);
					}
					target = copy;
				}
			}
		} catch (ClassNotFoundException exception) {
			LoggerService.getInstance().getLogger().severe("- An issue occurred while attempting to deserialize object " + type.getTypeName());
			exception.printStackTrace();
		}
		return target;
	}

	@Override
	protected Object get(String key) {
		String[] a = key.split("\\.");
		String k = a[Math.max(0, a.length - 1)];
		JSONObject o = json;
		for (int i = 0; i < a.length - 1; i++) {
			String pathKey = a[i];
			Object obj = o.get(pathKey);
			if (obj instanceof JSONObject) {
				JSONObject js = (JSONObject) obj;
				if (js.containsKey(k)) {
					return js.get(k);
				} else {
					o = js;
				}
			} else {
				return obj;
			}
		}
		return o.get(k);
	}

	@Override
	protected <T> T get(String key, Class<T> type) {
		boolean stop = false;
		Object ob = null;
		String[] a = key.split("\\.");
		String k = a[Math.max(0, a.length - 1)];
		JSONObject o = json;
		for (int i = 0; i < a.length - 1; i++) {
			String pathKey = a[i];
			Object obj = o.get(pathKey);
			if (obj instanceof JSONObject) {
				JSONObject js = (JSONObject) obj;
				if (js.containsKey(k)) {
					ob = checkObject(type, false, js.get(k));
					stop = true;
				} else {
					o = js;
				}
			} else {
				ob = checkObject(type.isArray() ? type.getComponentType() : type, obj instanceof JSONArray, obj);
				stop = true;
			}
		}
		if (!stop) {
			Object object = o.get(k);
			ob = checkObject(type.isArray() ? type.getComponentType() : type, (object instanceof JSONArray), object);
		}
		if (ob == null) return null;
		if (!type.isArray() && !type.isAssignableFrom(ob.getClass())) return null;
		return type.cast(ob);
	}

	@Override
	public com.github.sanctum.panther.file.Node getNode(String key) {
		return (com.github.sanctum.panther.file.Node) memory.entrySet().stream().filter(n -> n.getKey().equals(key)).map(Map.Entry::getValue).findFirst().orElseGet(() -> {
			Node n = new Node(key, this);
			memory.put(n.getPath(), n);
			return n;
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getKeys(boolean deep) {
		Set<String> keys;
		if (deep) {
			return MapDecompressionService.getInstance().decompress((Set<Map.Entry<String, Object>>)json.entrySet(), '.', null).toSet();
		} else {
			keys = new HashSet<>((Set<String>) json.keySet());
		}
		return keys;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getValues(boolean deep) {
		Map<String, Object> map = new HashMap<>();
		if (deep) {
			return MapDecompressionService.getInstance().decompress((Set<Map.Entry<String, Object>>)json.entrySet(), '.', null).toMap();
		} else {
			json.entrySet().forEach(e -> {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>)e;
				map.put(entry.getKey(), entry.getValue());
			});
		}
		return map;
	}

	@Override
	public String getString(String key) {
		Object o = get(key);
		return String.valueOf(o);
	}

	@Override
	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(getString(key));
	}

	@Override
	public boolean isList(String key) {
		return get(key) instanceof List;
	}

	@Override
	public boolean isStringList(String key) {
		return !getStringList(key).isEmpty();
	}

	@Override
	public boolean isFloatList(String key) {
		return !getFloatList(key).isEmpty();
	}

	@Override
	public boolean isDoubleList(String key) {
		return !getDoubleList(key).isEmpty();
	}

	@Override
	public boolean isLongList(String key) {
		return !getLongList(key).isEmpty();
	}

	@Override
	public boolean isIntegerList(String key) {
		return !getIntegerList(key).isEmpty();
	}

	@Override
	public boolean isBoolean(String key) {
		return get(key) instanceof Boolean;
	}

	@Override
	public boolean isDouble(String key) {
		return get(key) instanceof Double;
	}

	@Override
	public boolean isInt(String key) {
		return get(key) instanceof Integer;
	}

	@Override
	public boolean isLong(String key) {
		return get(key) instanceof Long;
	}

	@Override
	public boolean isFloat(String key) {
		return get(key) instanceof Float;
	}

	@Override
	public boolean isString(String key) {
		return get(key) instanceof String;
	}

	@Override
	public String getPath() {
		String s = "/" + getName() + "/";
		if (getDirectory() != null) {
			s = s + getDirectory();
		}
		return s;
	}

	@Override
	public boolean isNode(String key) {
		String[] a = key.split("\\.");
		String k = a[Math.max(0, a.length - 1)];
		JSONObject o = json;
		for (int i = 0; i < a.length - 1; i++) {
			String pathKey = a[i];
			Object obj = o.get(pathKey);
			if (obj instanceof JSONObject) {
				JSONObject js = (JSONObject) obj;
				if (js.containsKey(k)) {
					return js.get(k) instanceof JSONObject;
				} else {
					o = js;
				}
			}
		}
		return o.get(k) instanceof JSONObject;
	}

	@Override
	public double getDouble(String key) {
		try {
			return Double.parseDouble(getString(key));
		} catch (Exception ignored) {
		}
		return 0.0;
	}

	@Override
	public long getLong(String key) {
		try {
			return Long.parseLong(getString(key));
		} catch (Exception ignored) {
		}
		return 0L;
	}

	@Override
	public float getFloat(String key) {
		try {
			return Float.parseFloat(getString(key));
		} catch (Exception ignored) {
		}
		return 0.0f;
	}

	@Override
	public int getInt(String key) {
		try {
			return Integer.parseInt(getString(key));
		} catch (Exception ignored) {
		}
		return 0;
	}

	@Override
	public Map<?, ?> getMap(String key) {
		Object o = get(key);
		if (o instanceof Map) {
			return (Map<?, ?>) o;
		}
		return new HashMap<>();
	}

	@Override
	public List<?> getList(String key) {
		Object o = get(key);
		if (o instanceof List) {
			return (List<?>) o;
		}
		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStringList(String key) {
		List<?> l = getList(key);
		if (l.isEmpty()) return new ArrayList<>();
		if (!(l.get(0) instanceof String)) return new ArrayList<>();
		return (List<String>) l;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getIntegerList(String key) {
		List<?> l = getList(key);
		if (l.isEmpty()) return new ArrayList<>();
		if (!(l.get(0) instanceof Integer) || !(l.get(0) instanceof Long)) return new ArrayList<>();
		return (List<Integer>) l;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Double> getDoubleList(String key) {
		List<?> l = getList(key);
		if (l.isEmpty()) return new ArrayList<>();
		if (!(l.get(0) instanceof Double) || !(l.get(0) instanceof Float)) return new ArrayList<>();
		return (List<Double>) l;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Float> getFloatList(String key) {
		List<?> l = getList(key);
		if (l.isEmpty()) return new ArrayList<>();
		if (!(l.get(0) instanceof Float) || !(l.get(0) instanceof Double)) return new ArrayList<>();
		return (List<Float>) l;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getLongList(String key) {
		List<?> l = getList(key);
		if (l.isEmpty()) return new ArrayList<>();
		if (!(l.get(0) instanceof Long) || !(l.get(0) instanceof Integer)) return new ArrayList<>();
		return (List<Long>) l;
	}

	@Override
	public Extension getType() {
		return Type.JSON;
	}
}
