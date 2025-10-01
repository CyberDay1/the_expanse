package net.neoforged.neoforge.common;

import java.util.function.Supplier;

public class ModConfigSpec {
    public static class Builder {
        public Builder push(String path) {
            return this;
        }

        public Builder pop() {
            return this;
        }

        public Builder comment(String... comment) {
            return this;
        }

        public BooleanValue define(String key, boolean defaultValue) {
            return new BooleanValue(defaultValue);
        }

        public ConfigValue<String> define(String key, String defaultValue) {
            return new ConfigValue<>(defaultValue);
        }

        public DoubleValue defineInRange(String key, double defaultValue, double minValue, double maxValue) {
            double clamped = Math.max(minValue, Math.min(maxValue, defaultValue));
            return new DoubleValue(clamped);
        }

        public ModConfigSpec build() {
            return new ModConfigSpec();
        }
    }

    public static class BooleanValue implements Supplier<Boolean> {
        private boolean value;

        private BooleanValue(boolean value) {
            this.value = value;
        }

        @Override
        public Boolean get() {
            return value;
        }

        public boolean getAsBoolean() {
            return value;
        }

        public void set(boolean value) {
            this.value = value;
        }
    }

    public static class DoubleValue implements Supplier<Double> {
        private double value;

        private DoubleValue(double value) {
            this.value = value;
        }

        @Override
        public Double get() {
            return value;
        }

        public double getAsDouble() {
            return value;
        }

        public void set(double value) {
            this.value = value;
        }
    }

    public static class ConfigValue<T> implements Supplier<T> {
        private T value;

        private ConfigValue(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }
}
