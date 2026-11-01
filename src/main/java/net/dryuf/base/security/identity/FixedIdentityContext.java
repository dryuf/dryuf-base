package net.dryuf.base.security.identity;

import lombok.Getter;
import lombok.NonNull;
import net.dryuf.base.locale.LocaleContext;

import java.security.Permission;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


/**
 * Identity of user in the system.  The purpose is to provide user information with its roles.
 */
public class FixedIdentityContext implements IdentityContext
{
	@Getter
	final LocaleContext localeContext;

	@Getter
	final Principal principal;

	@Getter
	final Set<String> roles;

	@Getter
	final boolean isAnonymous;

	@Getter
	final Map<String, Object> attributes;

	@Getter
	final Set<Permission> permissions;

	public FixedIdentityContext(
		final Principal principal,
		@NonNull LocaleContext localeContext,
		@NonNull Set<String> roles,
		final boolean isAnonymous,
		@NonNull Map<String, Object> attributes,
		@NonNull Set<Permission> permissions
	) {
		this.principal = principal;
		this.localeContext = localeContext;
		this.roles = roles;
		this.isAnonymous = isAnonymous;
		this.attributes = attributes;
		this.permissions = permissions;
	}

	public FixedIdentityContext(final Principal principal, @NonNull LocaleContext localeContext, @NonNull Set<String> roles, final boolean isAnonymous) {
		this(principal, localeContext, roles, isAnonymous, Map.of(), Set.of());
	}

	public boolean hasRole(@NonNull String role) {
		return roles.contains(role);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(@NonNull String name) {
		return (T) attributes.get(name);
	}

	public CompletionStage<Boolean> checkPermission(Permission permission) {
		return CompletableFuture.completedFuture(permissions.stream().anyMatch(p -> p.implies(permission)));
	}
}
