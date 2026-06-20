package com.lumoren.dglabcraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DGLabCraftTest {

    @Test
    void forgeCanFindNoArgModConstructor() {
        assertDoesNotThrow(() -> DGLabCraft.class.getDeclaredConstructor());
    }
}
