package sm.system.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IpAddressRangeTests {
    @Test
    void parsesAndNormalizesStrictIpv4AndIpv6Ranges() {
        assertEquals("10.10.0.0/16", IpAddressRange.parse("10.10.2.3/16").normalized());
        assertEquals("2001:db8::/32", IpAddressRange.parse("2001:0db8::1/32").normalized());
        assertEquals("192.0.2.8", IpAddressRange.parse("192.0.2.8").normalized());
    }

    @Test
    void matchesOnlyAddressesFromTheSameAddressFamilyAndSubnet() {
        IpAddressRange ipv4Range = IpAddressRange.parse("10.10.0.0/16");
        assertTrue(ipv4Range.contains("10.10.2.3"));
        assertFalse(ipv4Range.contains("10.11.2.3"));
        assertFalse(ipv4Range.contains("2001:db8::1"));
    }

    @Test
    void rejectsNamesZonesPortsWildcardsRangesAndNonCanonicalIpv4Syntax() {
        assertThrows(IllegalArgumentException.class, () -> IpAddressRange.parse("localhost"));
        assertThrows(IllegalArgumentException.class, () -> IpAddressRange.parse("fe80::1%eth0"));
        assertThrows(IllegalArgumentException.class, () -> IpAddressRange.parse("192.0.2.1:8080"));
        assertThrows(IllegalArgumentException.class, () -> IpAddressRange.parse("10.*.*.*"));
        assertThrows(IllegalArgumentException.class, () -> IpAddressRange.parse("10.0.0.1-10.0.0.8"));
        assertThrows(IllegalArgumentException.class, () -> IpAddressRange.parse("010.0.0.1"));
        assertFalse(IpAddressRange.isAddressLiteral("attacker.example"));
    }
}
