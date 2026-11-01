package net.dryuf.base.security.identity;

import net.dryuf.base.locale.BasicLocaleContext;
import org.testng.annotations.Test;

import java.security.Permission;
import java.security.Principal;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;


public class RolesIdentityContextTest
{
	private static final Principal PRINCIPAL = new FixedIdentityContextTest.UserPrincipal("joe");
	private static final Permission READ = new RuntimePermission("read");
	private static final Permission WRITE = new RuntimePermission("write");

	private final FixedIdentityContext parent = new FixedIdentityContext(
		PRINCIPAL,
		BasicLocaleContext.builder().locale(Locale.ROOT).timezone(ZoneOffset.UTC).currency(Currency.getInstance("EUR")).domain("default").build(),
		Set.of("user"),
		true,
		Map.of("age", 42),
		Set.of(READ)
	);

	@Test
	public void getRoles_added_merged()
	{
		RolesIdentityContext identity = new RolesIdentityContext(parent, Set.of("admin", "auditor"));

		assertEquals(identity.getRoles(), Set.of("user", "admin", "auditor"));
	}

	@Test
	public void getRoles_overlapping_deduplicated()
	{
		RolesIdentityContext identity = new RolesIdentityContext(parent, Set.of("user", "admin"));

		assertEquals(identity.getRoles(), Set.of("user", "admin"));
	}

	@Test
	public void getRoles_noneAdded_sameAsParent()
	{
		RolesIdentityContext identity = new RolesIdentityContext(parent, Set.of());

		assertEquals(identity.getRoles(), Set.of("user"));
	}

	@Test
	public void getRoles_modify_throws()
	{
		RolesIdentityContext identity = new RolesIdentityContext(parent, Set.of("admin"));

		expectThrows(UnsupportedOperationException.class, () -> identity.getRoles().add("root"));
	}

	@Test
	public void hasRole_parentAndAdded_true()
	{
		RolesIdentityContext identity = new RolesIdentityContext(parent, Set.of("admin"));

		assertTrue(identity.hasRole("user"));
		assertTrue(identity.hasRole("admin"));
		assertFalse(identity.hasRole("guest"));
	}

	@Test
	public void hasRole_added_parentUnchanged()
	{
		new RolesIdentityContext(parent, Set.of("admin"));

		assertFalse(parent.hasRole("admin"));
		assertEquals(parent.getRoles(), Set.of("user"));
	}

	@Test
	public void nested_multipleLayers_allRolesMerged()
	{
		RolesIdentityContext identity = new RolesIdentityContext(new RolesIdentityContext(parent, Set.of("admin")), Set.of("auditor"));

		assertEquals(identity.getRoles(), Set.of("user", "admin", "auditor"));
		assertTrue(identity.hasRole("admin"));
	}

	@Test
	public void delegated_principalAndFlags_fromParent()
	{
		RolesIdentityContext identity = new RolesIdentityContext(parent, Set.of("admin"));

		assertSame(identity.getPrincipal(), PRINCIPAL);
		assertSame(identity.getPrincipal(FixedIdentityContextTest.UserPrincipal.class), PRINCIPAL);
		assertTrue(identity.isAnonymous());
	}

	@Test
	public void delegated_attributes_fromParent()
	{
		RolesIdentityContext identity = new RolesIdentityContext(parent, Set.of("admin"));

		Integer age = identity.getAttribute("age");

		assertEquals(age, Integer.valueOf(42));
		assertEquals(identity.getAttributes(), Map.of("age", 42));
	}

	@Test
	public void delegated_permissions_fromParent()
	{
		RolesIdentityContext identity = new RolesIdentityContext(parent, Set.of("admin"));

		assertTrue(identity.checkPermission(READ).toCompletableFuture().join());
		assertTrue(identity.checkPermissionBlocking(READ));
		assertFalse(identity.checkPermissionBlocking(WRITE));
	}
}
