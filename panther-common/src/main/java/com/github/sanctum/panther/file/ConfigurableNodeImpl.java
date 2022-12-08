package com.github.sanctum.panther.file;


import com.github.sanctum.panther.util.MapDecompression;
import com.github.sanctum.panther.util.OrdinalProcedure;
import com.github.sanctum.panther.util.PantherLogger;
import com.github.sanctum.panther.util.SimpleAsynchronousTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class ConfigurableNodeImpl implements com.github.sanctum.panther.file.Node, Primitive {

    protected final Configurable config;
    protected final String key;

    ConfigurableNodeImpl(String key, Configurable configuration) {
        this.config = configuration;
        this.key = key;
    }

    @Override
    public boolean isNode(String key) {
        return config.isNode(this.key + "." + key);
    }

    @Override
    public Node getNode(String node) {
        return (com.github.sanctum.panther.file.Node) Optional.ofNullable(config.memory.get(this.key + "." + node)).orElseGet(() -> {
            Node n = new ConfigurableNodeImpl(key + "." + node, config);
            config.memory.put(n.getPath(), n);
            return n;
        });
    }

    @Override
    public Object get() {
        return config.get(key);
    }

    @Override
    public Primitive toPrimitive() {
        return this;
    }

    @Override
    public <T extends Generic> T toGeneric(@NotNull Class<T> clazz) {
        T gen = (T) config.processors.get(clazz);
        if (gen != null) {
            OrdinalProcedure.of(gen).get(20, this).get();
            return gen;
        }
        throw new NullPointerException(clazz + " does not have a registered instance within this configurable!");
    }

    @Override
    public String getString() {
        return config.getString(this.key);
    }

    @Override
    public int getInt() {
        return config.getInt(this.key);
    }

    @Override
    public boolean getBoolean() {
        return config.getBoolean(this.key);
    }

    @Override
    public double getDouble() {
        return config.getDouble(this.key);
    }

    @Override
    public float getFloat() {
        return config.getFloat(this.key);
    }

    @Override
    public long getLong() {
        return config.getLong(this.key);
    }

    @Override
    public List<?> getList() {
        return config.getList(this.key);
    }

    @Override
    public Map<?, ?> getMap() {
        return config.getMap(this.key);
    }

    @Override
    public List<String> getStringList() {
        return config.getStringList(this.key);
    }

    @Override
    public List<Integer> getIntegerList() {
        return config.getIntegerList(this.key);
    }

    @Override
    public List<Double> getDoubleList() {
        return config.getDoubleList(this.key);
    }

    @Override
    public List<Float> getFloatList() {
        return config.getFloatList(this.key);
    }

    @Override
    public List<Long> getLongList() {
        return config.getLongList(this.key);
    }

    @Override
    public boolean isString() {
        return config.isString(this.key);
    }

    @Override
    public boolean isBoolean() {
        return config.isBoolean(this.key);
    }

    @Override
    public boolean isInt() {
        return config.isInt(this.key);
    }

    @Override
    public boolean isDouble() {
        return config.isDouble(this.key);
    }

    @Override
    public boolean isFloat() {
        return config.isFloat(this.key);
    }

    @Override
    public boolean isLong() {
        return config.isLong(this.key);
    }

    @Override
    public boolean isList() {
        return config.isList(this.key);
    }

    @Override
    public boolean isStringList() {
        return config.isStringList(this.key);
    }

    @Override
    public boolean isFloatList() {
        return config.isFloatList(this.key);
    }

    @Override
    public boolean isDoubleList() {
        return config.isDoubleList(this.key);
    }

    @Override
    public boolean isIntegerList() {
        return config.isIntegerList(this.key);
    }

    @Override
    public boolean isLongList() {
        return config.isLongList(this.key);
    }

    @Override
    public <T> T get(Class<T> type) {
        Object o = config.get(this.key, type);
        if (o != null) {
            return (T) o;
        }
        return null;
    }

    @Override
    public String getPath() {
        return this.key;
    }

    @Override
    public boolean delete() {
        if (config.isNode(this.key)) {
            config.set(this.key, null);
            SimpleAsynchronousTask.runNow(() -> config.memory.remove(this.key));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reload() {
        config.reload();
    }

    @Override
    public boolean create() {
        if (!config.exists()) {
            try {
                config.create();
            } catch (IOException ex) {
                PantherLogger.getInstance().getLogger().severe("- An issue occurred while attempting to create the backing file for the '" + config.getName() + "' configuration.");
                ex.printStackTrace();
            }
        }
        if (config.getType() == Configurable.Type.JSON) {
            set(new Object());
        }
        save();
        return false;
    }

    @Override
    public boolean exists() {
        return isNode(this.key) || get() != null;
    }

    @Override
    public boolean save() {
        return config.save();
    }

    @Override
    public void set(Object o) {
        config.set(this.key, o);
    }

    @Override
    public com.github.sanctum.panther.file.Node getParent() {
        String[] k = this.key.split("//.");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < k.length - 1; i++) {
            builder.append(k[i]).append(".");
        }
        String key = builder.toString();
        if (key.endsWith(".")) {
            key = key.substring(0, builder.length() - 1);
        }
        if (key.equals(this.key)) return this;
        return getNode(key);
    }

    @Override
    public String toJson() {
        return JsonAdapter.getJsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().serializeNulls().setLenient().serializeSpecialFloatingPointValues().create().toJson(get());
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        Set<String> keys = new HashSet<>();
        if (config.get(this.key) instanceof Map) {
            Map<String, Object> level1 = (Map<String, Object>) config.get(this.key);
            if (deep) {
                return MapDecompression.getInstance().decompress(level1.entrySet(), '.', null).toSet();
            } else {
                keys.addAll(level1.keySet());
            }
        } else {
            keys.add(this.key);
        }
        return keys;
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        Map<String, Object> map = new HashMap<>();
        if (config.get(this.key) instanceof Map) {
            Map<String, Object> level1 = (Map<String, Object>) config.get(this.key);
            if (deep) {
                return MapDecompression.getInstance().decompress(level1.entrySet(), '.', null).toMap();
            } else {
                map.putAll(level1);
            }
        } else {
            map.put(this.key, get());
        }
        return map;
    }

}
