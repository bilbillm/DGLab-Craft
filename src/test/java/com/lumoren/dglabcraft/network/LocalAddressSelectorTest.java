package com.lumoren.dglabcraft.network;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalAddressSelectorTest {
    @Test
    void prefersPhysicalLanOverVirtualAdapter() {
        var vmware = new LocalAddressSelector.Candidate("192.168.6.1", "eth7", "VMware Network Adapter VMnet1", true, false);
        var wlan = new LocalAddressSelector.Candidate("192.168.31.25", "wlan0", "Intel Wi-Fi", false, true);

        assertEquals("192.168.31.25", LocalAddressSelector.chooseBestLocalIpAddress(List.of(vmware, wlan)));
    }

    @Test
    void ignoresPublicAndLoopbackAddresses() {
        var publicAddress = new LocalAddressSelector.Candidate("8.8.8.8", "eth0", "Ethernet", false, true);
        var loopback = new LocalAddressSelector.Candidate("127.0.0.1", "lo", "Loopback", false, false);

        assertNull(LocalAddressSelector.chooseBestLocalIpAddress(List.of(publicAddress, loopback)));
    }

    @Test
    void detectsPrivateIpv4Ranges() {
        assertTrue(LocalAddressSelector.isPrivateIpv4("10.0.0.8"));
        assertTrue(LocalAddressSelector.isPrivateIpv4("172.16.1.1"));
        assertTrue(LocalAddressSelector.isPrivateIpv4("172.31.1.1"));
        assertTrue(LocalAddressSelector.isPrivateIpv4("192.168.1.9"));
        assertFalse(LocalAddressSelector.isPrivateIpv4("172.32.1.1"));
        assertFalse(LocalAddressSelector.isPrivateIpv4("8.8.8.8"));
    }
}
