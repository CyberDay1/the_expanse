package com.theexpanse.parity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OreScalerParityTest {
    private static final String CLASS_NAME = "com.theexpanse.worldgen.OreScaler";

    @Test
    void oreScalerConstantsMatchExpectedValues() throws ReflectiveOperationException {
        Class<?> oreScaler = Class.forName(CLASS_NAME);
        Map<String, Integer> expected = Map.of(
            "NEW_MIN", -256,
            "NEW_MAX", 2288,
            "VANILLA_RANGE", 384,
            "VANILLA_MIN", -64,
            "VANILLA_MAX", 320
        );

        for (Map.Entry<String, Integer> entry : expected.entrySet()) {
            Field field = oreScaler.getDeclaredField(entry.getKey());
            field.setAccessible(true);
            Object value = field.get(null);
            assertEquals(entry.getValue(), value,
                () -> "Unexpected value for " + entry.getKey() + " in " + oreScaler.getName());
        }
    }

    @Test
    void oreScalerHeightScalingRemainsStable() throws ReflectiveOperationException {
        Class<?> oreScaler = Class.forName(CLASS_NAME);
        Method method = oreScaler.getDeclaredMethod("scaleY", int.class);
        method.setAccessible(true);
        IntUnaryOperator scale = value -> invokeScale(method, value);

        assertEquals(-256, scale.applyAsInt(-64), "Vanilla min should scale to new minimum height");
        assertEquals(0, scale.applyAsInt(0), "Zero height should remain zero after scaling");
        assertEquals(1525, scale.applyAsInt(256), "Mid-range height scaling drift detected");
        assertEquals(1907, scale.applyAsInt(320), "Upper vanilla band scaled incorrectly");
        assertEquals(2288, scale.applyAsInt(384), "Vanilla top cap should clamp to new maximum");
    }

    private static int invokeScale(Method method, int value) {
        try {
            Object result = method.invoke(null, value);
            assertNotNull(result, "Scale method returned null");
            assertTrue(result instanceof Integer, "Scale method did not return an integer");
            return (Integer) result;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to invoke OreScaler#scaleY", exception);
        }
    }
}
