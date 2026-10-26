package net.dryuf.base.security.identity;

import org.testng.annotations.Test;

import java.security.Permission;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;


public class FixedIdentityContextTest
{
	private static final Principal PRINCIPAL = new UserPrincipal("joe");
	private static final Permission READ = new RuntimePermission("read");
	private static final Permission WRITE = new RuntimePermission("write");

	@Test
	public void constructor_basic_defaultsEmpty()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of("user"), false);

		assertSame(identity.getPrincipal(), PRINCIPAL);
		assertEquals(identity.getRoles(), Set.of("user"));
		assertFalse(identity.isAnonymous());
		assertEquals(identity.getAttributes(), Map.of());
		assertEquals(identity.getPermissions(), Set.of());
	}

	@Test
	public void isAnonymous_anonymous_true()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of(), true);

		assertTrue(identity.isAnonymous());
	}

	@Test
	public void getPrincipal_matchingClass_returned()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of(), false);

		UserPrincipal principal = identity.getPrincipal(UserPrincipal.class);

		assertSame(principal, PRINCIPAL);
		assertEquals(principal.getName(), "joe");
	}

	@Test
	public void getPrincipal_otherClass_throws()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of(), false);

		expectThrows(ClassCastException.class, () -> identity.getPrincipal(OtherPrincipal.class));
	}

	@Test
	public void hasRole_presentAndMissing_matched()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of("user", "admin"), false);

		assertTrue(identity.hasRole("user"));
		assertTrue(identity.hasRole("admin"));
		assertFalse(identity.hasRole("guest"));
	}

	@Test
	public void getAttribute_present_returnedTyped()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of(), false, Map.of("age", 42, "name", "Joe"), Set.of());

		Integer age = identity.getAttribute("age");
		String name = identity.getAttribute("name");

		assertEquals(age, Integer.valueOf(42));
		assertEquals(name, "Joe");
		assertEquals(identity.getAttributes(), Map.of("age", 42, "name", "Joe"));
	}

	@Test
	public void getAttribute_missing_null()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of(), false, Map.of("age", 42), Set.of());

		assertNull(identity.getAttribute("name"));
	}

	@Test
	public void getAttribute_wrongType_throwsOnUse()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of(), false, Map.of("age", 42), Set.of());

		expectThrows(ClassCastException.class, () -> {
			String age = identity.getAttribute("age");
		});
	}

	@Test
	public void checkPermission_granted_true()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of(), false, Map.of(), Set.of(READ));

		assertTrue(identity.checkPermission(new RuntimePermission("read")).toCompletableFuture().join());
		assertTrue(identity.checkPermissionBlocking(READ));
	}

	@Test
	public void checkPermission_notGranted_false()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of(), false, Map.of(), Set.of(READ));

		assertFalse(identity.checkPermission(WRITE).toCompletableFuture().join());
		assertFalse(identity.checkPermissionBlocking(WRITE));
	}

	@Test
	public void checkPermission_noPermissions_false()
	{
		FixedIdentityContext identity = new FixedIdentityContext(PRINCIPAL, Set.of("admin"), false);

		assertFalse(identity.checkPermissionBlocking(READ));
	}

	record UserPrincipal(String name) implements Principal
	{
		@Override
		public String getName()
		{
			return name;
		}
	}

	record OtherPrincipal(String name) implements Principal
	{
		@Override
		public String getName()
		{
			return name;
		}
	}
}
