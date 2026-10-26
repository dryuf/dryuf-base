package net.dryuf.base.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Network address utilities.
 */
public class AddressUtil
{
	/**
	 * Socket address pattern: {@code host[:port]} or {@code [ipv6][:port]}.  Address consisting of digits only
	 * (except single {@code 0}) is rejected, as it is likely port without host.
	 */
	private static final Pattern ADDRESS_PATTERN = Pattern.compile(
		"^(?!(?!0$)\\d+$)" +
		"(?:\\[(?<ipv6>[0-9A-Fa-f.]*:[0-9A-Fa-f:.]*(?:%[\\w.-]+)?)]|(?<host>[^:\\[\\]\\s]+))" +
		"(?::(?<port>\\d{1,5}))?$"
	);

	/**
	 * Parses socket address in form {@code host[:port]} or {@code [ipv6][:port]}, falling back to default port if
	 * port is not specified.
	 *
	 * The host part can be either hostname, IPv4 address or IPv6 address enclosed in square brackets.  Special
	 * host values {@code *} and {@code .} denote wildcard address (listening on all interfaces), resulting in
	 * {@link InetSocketAddress#InetSocketAddress(int)}.  IPv6 address is parsed as literal, without name
	 * resolution.  Otherwise, the result is created by {@link InetSocketAddress#InetSocketAddress(String, int)},
	 * which attempts to resolve the hostname immediately.  If resolution fails, the address is returned unresolved
	 * (see {@link InetSocketAddress#isUnresolved()}).
	 *
	 * Address consisting of digits only is rejected, as it is likely port without host.  The only exception is
	 * single {@code 0}, which is valid address (any local address).  Empty host is rejected too.
	 *
	 * Examples:
	 * <ul>
	 *     <li>{@code localhost:8080} - localhost, port 8080</li>
	 *     <li>{@code 192.168.1.1} - 192.168.1.1, default port</li>
	 *     <li>{@code [::1]:8080} - IPv6 loopback, port 8080</li>
	 *     <li>{@code [fe80::1%eth0]} - IPv6 link-local address with scope, default port</li>
	 *     <li>{@code *:8080} - wildcard address, port 8080</li>
	 *     <li>{@code .} - wildcard address, default port</li>
	 *     <li>{@code 0} - any local address, default port</li>
	 *     <li>{@code 8080}, {@code :8080}, {@code ::1}, {@code [localhost]} - invalid</li>
	 * </ul>
	 *
	 * @param address
	 *      address in form {@code host[:port]} or {@code [ipv6][:port]}
	 * @param defaultPort
	 *      port to use if address does not contain port
	 *
	 * @return
	 *      parsed socket address.
	 *
	 * @throws IllegalArgumentException
	 *      if the address cannot be parsed, it contains only port without host, IPv6 address is invalid or port
	 *      is outside of valid range 0 - 65535
	 */
	public static InetSocketAddress parseOptionalAddress(String address, int defaultPort) {
		Matcher matcher = ADDRESS_PATTERN.matcher(address);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(
				"Failed to parse socket address, expected host[:port] or [ipv6][:port] : " + address);
		}
		String portString = matcher.group("port");
		int port = portString != null ? Integer.parseInt(portString) : defaultPort;
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port out of range 0 - 65535 in socket address: " + address);
		}
		String ipv6 = matcher.group("ipv6");
		if (ipv6 != null) {
			try {
				return new InetSocketAddress(InetAddress.getByName(ipv6), port);
			}
			catch (UnknownHostException ex) {
				throw new IllegalArgumentException("Invalid IPv6 address in socket address: " + address, ex);
			}
		}
		String host = matcher.group("host");
		if (host.equals("*") || host.equals(".")) {
			return new InetSocketAddress(port);
		}
		return new InetSocketAddress(host, port);
	}

	/**
	 * Formats socket address as {@code ip:port}, always using numeric IP address, regardless of whether the
	 * address was created from hostname or not.
	 *
	 * IPv6 addresses are enclosed in square brackets, i.e. {@code [ip]:port}.  If the address is unresolved,
	 * there is no IP address available and the host string is used instead.  No name resolution is performed.
	 *
	 * Examples:
	 * <ul>
	 *     <li>{@code localhost/127.0.0.1:8080} - {@code 127.0.0.1:8080}</li>
	 *     <li>IPv6 loopback, port 8080 - {@code [0:0:0:0:0:0:0:1]:8080}</li>
	 *     <li>wildcard, port 8080 - {@code 0.0.0.0:8080}</li>
	 * </ul>
	 *
	 * @param address
	 *      socket address to format
	 *
	 * @return
	 *      formatted address in form {@code ip:port}.
	 */
	public static String formatIpAddress(InetSocketAddress address) {
		InetAddress ip = address.getAddress();
		String host = ip != null ? ip.getHostAddress() : address.getHostString();
		if (host.indexOf(':') >= 0) {
			return "[" + host + "]:" + address.getPort();
		}
		return host + ":" + address.getPort();
	}
}
