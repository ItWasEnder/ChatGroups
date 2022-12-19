package me.endergaming.chatgroups.utils;

import me.endergaming.chatgroups.groups.Options;

import java.util.EnumMap;

public class OptionsMap {
    private final EnumMap<Options, Value> map = new EnumMap<>(Options.class);

    public <T> void put(Options option, T value) {
        this.map.put(option, new Value(value));
    }

    public Value get(Options option) {
        return this.map.getOrDefault(option, new Value("invalid"));
    }

    public static class Value {
        final Object value;

        public Value(Object value) {
            this.value = value;
        }

        public boolean getBoolean() {
            return (boolean) this.value;
        }

        public int getInt() {
            return (int) this.value;
        }

        public String getString() {
            return (String) this.value;
        }

        public double getDouble() {
            return (double) this.value;
        }

        public float getFloat() {
            return (float) this.value;
        }

        public boolean isBoolean() {
            return Boolean.class.isAssignableFrom(this.value.getClass());
        }

        public boolean isInt() {
            return Integer.class.isAssignableFrom(this.value.getClass());
        }

        public boolean isString() {
            return String.class.isAssignableFrom(this.value.getClass());
        }

        public boolean isDouble() {
            return Double.class.isAssignableFrom(this.value.getClass());
        }

        public boolean isFloat() {
            return Float.class.isAssignableFrom(this.value.getClass());
        }
    }
}
