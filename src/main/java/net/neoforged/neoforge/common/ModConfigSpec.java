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

        public ModConfigSpec build() {
            return new ModConfigSpec();
        }
    }

    public static class BooleanValue implements Supplier<Boolean> {
        private final boolean value;

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
    }
}
