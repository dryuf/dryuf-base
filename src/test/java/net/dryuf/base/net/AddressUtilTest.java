package net.dryuf.base.net;

import org.testng.annotations.Test;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;


public class AddressUtilTest
{
	@Test
	public void parseOptionalAddress_whenHostAndPort_thenBoth()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("127.0.0.1:1234", 80);

		assertEquals(result.getHostString(), "127.0.0.1");
		assertEquals(result.getPort(), 1234);
		assertFalse(result.isUnresolved());
		assertTrue(result.getAddress().isLoopbackAddress());
	}

	@Test
	public void parseOptionalAddress_whenHostOnly_thenDefaultPort()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("127.0.0.1", 80);

		assertEquals(result.getHostString(), "127.0.0.1");
		assertEquals(result.getPort(), 80);
	}

	@Test
	public void parseOptionalAddress_whenHostname_thenResolved()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("localhost:8080", 80);

		assertEquals(result.getHostString(), "localhost");
		assertEquals(result.getPort(), 8080);
		assertFalse(result.isUnresolved());
		assertTrue(result.getAddress().isLoopbackAddress());
	}

	@Test
	public void parseOptionalAddress_whenStarWithPort_thenWildcard()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("*:8080", 80);

		assertTrue(result.getAddress().isAnyLocalAddress());
		assertEquals(result.getPort(), 8080);
	}

	@Test
	public void parseOptionalAddress_whenStarOnly_thenWildcardDefaultPort()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("*", 80);

		assertTrue(result.getAddress().isAnyLocalAddress());
		assertEquals(result.getPort(), 80);
	}

	@Test
	public void parseOptionalAddress_whenDotWithPort_thenWildcard()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress(".:8080", 80);

		assertTrue(result.getAddress().isAnyLocalAddress());
		assertEquals(result.getPort(), 8080);
	}

	@Test
	public void parseOptionalAddress_whenDotOnly_thenWildcardDefaultPort()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress(".", 80);

		assertTrue(result.getAddress().isAnyLocalAddress());
		assertEquals(result.getPort(), 80);
	}

	@Test
	public void parseOptionalAddress_whenUnresolvable_thenUnresolved()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("nonexistent.invalid:8080", 80);

		assertTrue(result.isUnresolved());
		assertEquals(result.getHostString(), "nonexistent.invalid");
		assertEquals(result.getPort(), 8080);
	}

	@Test
	public void parseOptionalAddress_whenInvalidPort_thenIllegalArgumentException()
	{
		IllegalArgumentException ex = expectThrows(IllegalArgumentException.class,
			() -> AddressUtil.parseOptionalAddress("localhost:abc", 80));

		assertEquals(ex.getClass(), IllegalArgumentException.class);
	}

	@Test
	public void parseOptionalAddress_whenTooLongPort_thenIllegalArgumentException()
	{
		IllegalArgumentException ex = expectThrows(IllegalArgumentException.class,
			() -> AddressUtil.parseOptionalAddress("localhost:99999999999", 80));

		assertEquals(ex.getClass(), IllegalArgumentException.class);
	}

	@Test
	public void parseOptionalAddress_whenEmptyPort_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("localhost:", 80));
	}

	@Test
	public void parseOptionalAddress_whenEmptyHost_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress(":8080", 80));
	}

	@Test
	public void parseOptionalAddress_whenEmpty_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("", 80));
	}

	@Test
	public void parseOptionalAddress_whenIpv6WithPort_thenParsed()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("[::1]:8080", 80);

		assertTrue(result.getAddress() instanceof Inet6Address);
		assertTrue(result.getAddress().isLoopbackAddress());
		assertEquals(result.getPort(), 8080);
	}

	@Test
	public void parseOptionalAddress_whenIpv6Only_thenDefaultPort()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("[2001:db8::1]", 80);

		assertEquals(result.getAddress().getHostAddress(), "2001:db8:0:0:0:0:0:1");
		assertEquals(result.getPort(), 80);
	}

	@Test
	public void parseOptionalAddress_whenIpv6Any_thenAnyAddress()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("[::]:8080", 80);

		assertTrue(result.getAddress().isAnyLocalAddress());
		assertEquals(result.getPort(), 8080);
	}

	@Test
	public void parseOptionalAddress_whenIpv4MappedIpv6_thenParsed()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("[::ffff:127.0.0.1]:8080", 80);

		assertTrue(result.getAddress().isLoopbackAddress());
		assertEquals(result.getPort(), 8080);
	}

	@Test
	public void parseOptionalAddress_whenIpv6WithoutBrackets_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("::1", 80));
	}

	@Test
	public void parseOptionalAddress_whenIpv6Unclosed_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("[::1:8080", 80));
	}

	@Test
	public void parseOptionalAddress_whenIpv6Invalid_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("[1:2:3]:8080", 80));
	}

	@Test
	public void parseOptionalAddress_whenHostnameInBrackets_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("[localhost]:8080", 80));
	}

	@Test
	public void parseOptionalAddress_whenIpv6InvalidPort_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("[::1]:abc", 80));
	}

	@Test
	public void parseOptionalAddress_whenIpv6PortOutOfRange_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("[::1]:65536", 80));
	}

	@Test
	public void parseOptionalAddress_whenPortOutOfRange_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("localhost:65536", 80));
	}

	@Test
	public void parseOptionalAddress_whenPortOnly_thenIllegalArgumentException()
	{
		IllegalArgumentException ex = expectThrows(IllegalArgumentException.class,
			() -> AddressUtil.parseOptionalAddress("8080", 80));

		assertTrue(ex.getMessage().contains("8080"), ex.getMessage());
	}

	@Test
	public void parseOptionalAddress_whenZero_thenAnyAddressDefaultPort()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("0", 80);

		assertTrue(result.getAddress().isAnyLocalAddress());
		assertEquals(result.getPort(), 80);
	}

	@Test
	public void parseOptionalAddress_whenZeroWithPort_thenAnyAddress()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("0:8080", 80);

		assertTrue(result.getAddress().isAnyLocalAddress());
		assertEquals(result.getPort(), 8080);
	}

	@Test
	public void parseOptionalAddress_whenMultipleZerosOnly_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("00", 80));
	}

	@Test
	public void parseOptionalAddress_whenDigitsWithPort_thenNumericHost()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("2130706433:8080", 80);

		assertEquals(result.getAddress().getHostAddress(), "127.0.0.1");
		assertEquals(result.getPort(), 8080);
	}

	@Test
	public void parseOptionalAddress_whenHostWithDigits_thenAccepted()
	{
		InetSocketAddress result = AddressUtil.parseOptionalAddress("127.0.0.1", 80);

		assertEquals(result.getHostString(), "127.0.0.1");
		assertEquals(result.getPort(), 80);
	}

	@Test
	public void parseOptionalAddress_whenDefaultPortOutOfRange_thenIllegalArgumentException()
	{
		expectThrows(IllegalArgumentException.class, () -> AddressUtil.parseOptionalAddress("localhost", -1));
	}

	@Test
	public void formatIpAddress_whenIpv4_thenIpPort()
	{
		String result = AddressUtil.formatIpAddress(new InetSocketAddress("127.0.0.1", 1234));

		assertEquals(result, "127.0.0.1:1234");
	}

	@Test
	public void formatIpAddress_whenHostname_thenIpPort() throws Exception
	{
		InetSocketAddress address = new InetSocketAddress(
			InetAddress.getByAddress("example.com", new byte[]{ 10, 1, 2, 3 }), 8080);

		String result = AddressUtil.formatIpAddress(address);

		assertEquals(result, "10.1.2.3:8080");
	}

	@Test
	public void formatIpAddress_whenResolvedLocalhost_thenNumeric()
	{
		InetSocketAddress address = AddressUtil.parseOptionalAddress("localhost:8080", 80);

		String result = AddressUtil.formatIpAddress(address);

		assertEquals(result, (address.getAddress() instanceof Inet6Address ?
			"[" + address.getAddress().getHostAddress() + "]" : address.getAddress().getHostAddress()) + ":8080");
		assertFalse(result.startsWith("localhost"));
	}

	@Test
	public void formatIpAddress_whenIpv6_thenBracketed() throws Exception
	{
		InetSocketAddress address = new InetSocketAddress(InetAddress.getByName("::1"), 8080);

		String result = AddressUtil.formatIpAddress(address);

		assertEquals(result, "[0:0:0:0:0:0:0:1]:8080");
	}

	@Test
	public void formatIpAddress_whenIpv6WithHostname_thenBracketedIp() throws Exception
	{
		InetSocketAddress address = new InetSocketAddress(InetAddress.getByAddress("ip6.example.com",
			new byte[]{ 0x20, 0x01, 0x0d, (byte) 0xb8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }), 443);

		String result = AddressUtil.formatIpAddress(address);

		assertEquals(result, "[2001:db8:0:0:0:0:0:1]:443");
	}

	@Test
	public void formatIpAddress_whenWildcard_thenAnyAddress()
	{
		InetSocketAddress address = new InetSocketAddress(8080);

		String result = AddressUtil.formatIpAddress(address);

		assertTrue(result.equals("0.0.0.0:8080") || result.equals("[0:0:0:0:0:0:0:0]:8080"), result);
	}

	@Test
	public void formatIpAddress_whenUnresolved_thenHostString()
	{
		String result = AddressUtil.formatIpAddress(InetSocketAddress.createUnresolved("nonexistent.invalid", 8080));

		assertEquals(result, "nonexistent.invalid:8080");
	}
}
