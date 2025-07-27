package com.github.sanctum.panther;

import com.github.sanctum.panther.util.EasyTypeAdapter;

public class ClanTest implements TestInt {

    Object value = "Test it works!";

    @Override
    public <R> R getValue(String test) {
        return new EasyTypeAdapter<R>(){}.cast(value);
    }
}
