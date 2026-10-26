package net.dryuf.base.security.identity;

import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Identity delegate with roles added.
 */
public class RolesIdentityContext implements IdentityContext
{
	@Delegate
	final IdentityContext parentIdentityContext;

	@Getter
	final Set<String> roles;

	public RolesIdentityContext(IdentityContext parentIdentityContext, Set<String> addedRoles) {
		this.parentIdentityContext = parentIdentityContext;
		roles = Stream.concat(parentIdentityContext.getRoles().stream(), addedRoles.stream())
			.collect(Collectors.toUnmodifiableSet());
	}

	public boolean hasRole(String role) {
		return roles.contains(role);
	}
}
