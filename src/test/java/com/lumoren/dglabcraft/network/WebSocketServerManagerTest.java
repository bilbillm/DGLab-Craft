package com.lumoren.dglabcraft.network;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

class WebSocketServerManagerTest {

    private static WebSocketServerManager manager;

    @BeforeAll
    static void setUp() throws Exception {
        // 使用 Unsafe 绕过构造器 — ModConfig 在测试环境不可用
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) f.get(null);
        manager = (WebSocketServerManager) unsafe.allocateInstance(WebSocketServerManager.class);
    }

    // ===== priorityOf =====

    @Test
    void priorityOf_damage_highest() {
        assertEquals(3, manager.priorityOf(WebSocketServerManager.EffectSource.DAMAGE));
    }

    @Test
    void priorityOf_heartbeat_medium() {
        assertEquals(2, manager.priorityOf(WebSocketServerManager.EffectSource.HEARTBEAT));
    }

    @Test
    void priorityOf_environment_lowest() {
        assertEquals(1, manager.priorityOf(WebSocketServerManager.EffectSource.ENVIRONMENT));
    }

    @Test
    void priorityOf_none_negative() {
        assertEquals(-1, manager.priorityOf(WebSocketServerManager.EffectSource.NONE));
    }

    @Test
    void priorityOf_order_maintained() {
        assertTrue(manager.priorityOf(WebSocketServerManager.EffectSource.DAMAGE)
                > manager.priorityOf(WebSocketServerManager.EffectSource.HEARTBEAT));
        assertTrue(manager.priorityOf(WebSocketServerManager.EffectSource.HEARTBEAT)
                > manager.priorityOf(WebSocketServerManager.EffectSource.ENVIRONMENT));
        assertTrue(manager.priorityOf(WebSocketServerManager.EffectSource.ENVIRONMENT)
                > manager.priorityOf(WebSocketServerManager.EffectSource.NONE));
    }

    // ===== getLeaseTicks =====

    @Test
    void getLeaseTicks_heartbeat() {
        assertEquals(55, manager.getLeaseTicks(WebSocketServerManager.EffectSource.HEARTBEAT, "low_health"));
    }

    @Test
    void getLeaseTicks_heartbeat_nullDetail() {
        assertEquals(55, manager.getLeaseTicks(WebSocketServerManager.EffectSource.HEARTBEAT, null));
    }

    @Test
    void getLeaseTicks_environment_nether() {
        assertEquals(55, manager.getLeaseTicks(WebSocketServerManager.EffectSource.ENVIRONMENT, "nether"));
    }

    @Test
    void getLeaseTicks_environment_end() {
        assertEquals(55, manager.getLeaseTicks(WebSocketServerManager.EffectSource.ENVIRONMENT, "end"));
    }

    @Test
    void getLeaseTicks_environment_portal() {
        assertEquals(40, manager.getLeaseTicks(WebSocketServerManager.EffectSource.ENVIRONMENT, "portal"));
    }

    @Test
    void getLeaseTicks_environment_powderSnow() {
        assertEquals(40, manager.getLeaseTicks(WebSocketServerManager.EffectSource.ENVIRONMENT, "powder_snow"));
    }

    @Test
    void getLeaseTicks_environment_default() {
        assertEquals(45, manager.getLeaseTicks(WebSocketServerManager.EffectSource.ENVIRONMENT, "unknown_env"));
    }

    @Test
    void getLeaseTicks_damage_onFire() {
        assertEquals(30, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "onfire"));
        assertEquals(30, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "onFire"));
    }

    @Test
    void getLeaseTicks_damage_inFire() {
        assertEquals(30, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "infire"));
    }

    @Test
    void getLeaseTicks_damage_lava() {
        assertEquals(30, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "lava"));
    }

    @Test
    void getLeaseTicks_damage_hotFloor() {
        assertEquals(30, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "hotfloor"));
    }

    @Test
    void getLeaseTicks_damage_drown() {
        assertEquals(30, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "drown"));
    }

    @Test
    void getLeaseTicks_damage_freeze() {
        assertEquals(30, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "freeze"));
    }

    @Test
    void getLeaseTicks_damage_default() {
        assertEquals(12, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "mob"));
        assertEquals(12, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "arrow"));
        assertEquals(12, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "unknown_damage"));
        assertEquals(12, manager.getLeaseTicks(WebSocketServerManager.EffectSource.DAMAGE, null));
    }

    @Test
    void getLeaseTicks_none() {
        assertEquals(0, manager.getLeaseTicks(WebSocketServerManager.EffectSource.NONE, "any"));
        assertEquals(0, manager.getLeaseTicks(WebSocketServerManager.EffectSource.NONE, null));
    }

    // ===== parseStrengthMessage =====

    @Test
    void parseStrengthMessage_newDualFormat() {
        manager.parseStrengthMessage("strength-0+0+30+10");
        assertEquals(30, manager.getAppAMaxStrength());
        assertEquals(10, manager.getAppBMaxStrength());
    }

    @Test
    void parseStrengthMessage_newDualFormat_maxValues() {
        manager.parseStrengthMessage("strength-0+0+100+200");
        assertEquals(100, manager.getAppAMaxStrength());
        assertEquals(200, manager.getAppBMaxStrength());
    }

    @Test
    void parseStrengthMessage_oldFormat_channel1() {
        manager.parseStrengthMessage("strength-1+2+50");
        assertEquals(50, manager.getAppAMaxStrength());
    }

    @Test
    void parseStrengthMessage_oldFormat_channel2() {
        manager.parseStrengthMessage("strength-2+2+75");
        assertEquals(75, manager.getAppBMaxStrength());
    }

    @Test
    void parseStrengthMessage_oldFormat_modeIgnoredForStrength() {
        manager.parseStrengthMessage("strength-1+0+60");
        assertEquals(60, manager.getAppAMaxStrength());
    }

    @Test
    void parseStrengthMessage_garbageInput_noCrash() {
        assertDoesNotThrow(() -> manager.parseStrengthMessage("garbage"));
        assertDoesNotThrow(() -> manager.parseStrengthMessage("strength-xyz"));
        assertDoesNotThrow(() -> manager.parseStrengthMessage("strength-1+2"));
        assertDoesNotThrow(() -> manager.parseStrengthMessage(""));
    }

    @Test
    void parseStrengthMessage_nonStrength_noop() {
        int aBefore = manager.getAppAMaxStrength();
        int bBefore = manager.getAppBMaxStrength();
        manager.parseStrengthMessage("pulse-A:[\"0A0A0A0A0A0A0A0A\"]");
        assertEquals(aBefore, manager.getAppAMaxStrength());
        assertEquals(bBefore, manager.getAppBMaxStrength());
    }

    // ===== chooseBestLocalIpAddress =====

    @Test
    void chooseBestLocalIpAddress_prefersPhysicalLanOverVirtualAdapter() {
        var vmwareAddress = new WebSocketServerManager.LocalAddressCandidate(
            "192.168.6.1",
            "eth7",
            "VMware Network Adapter VMnet1",
            true,
            false
        );
        var wlanAddress = new WebSocketServerManager.LocalAddressCandidate(
            "192.168.31.25",
            "wlan0",
            "Intel(R) Wi-Fi 6 AX201",
            false,
            true
        );

        assertEquals("192.168.31.25", WebSocketServerManager.chooseBestLocalIpAddress(List.of(vmwareAddress, wlanAddress)));
    }

    @Test
    void chooseBestLocalIpAddress_ignoresPublicAndLoopbackAddresses() {
        var publicAddress = new WebSocketServerManager.LocalAddressCandidate(
            "8.8.8.8",
            "eth0",
            "Ethernet",
            false,
            true
        );
        var loopbackAddress = new WebSocketServerManager.LocalAddressCandidate(
            "127.0.0.1",
            "lo",
            "Loopback",
            false,
            false
        );

        assertNull(WebSocketServerManager.chooseBestLocalIpAddress(List.of(publicAddress, loopbackAddress)));
    }
}
