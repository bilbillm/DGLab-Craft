package com.lumoren.dglabcraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DGLabCraftConstructorTest {
    @Test
    void forgeModEntrypointHasNoArgConstructor() {
        assertDoesNotThrow(() -> DGLabCraft.class.getDeclaredConstructor());
    }
}
