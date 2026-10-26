package net.dryuf.base.security.identity;

import java.security.Permission;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;


/**
 * Identity of user in the system.  The purpose is to provide user information with its roles.
 */
public interface IdentityContext
{
	/**
	 * @return the {@link Principal} representing the current security identity.
	 */
	Principal getPrincipal();

	/**
	 * @param clazz {@link Principal} subclass
	 * @return the {@link Principal} subclass representing the current security identity.
	 */
	default <T extends Principal> T getPrincipal(Class<T> clazz) {
		return clazz.cast(getPrincipal());
	}

	/**
	 * @return <code>true</code> if this identity is anonymous
	 */
	boolean isAnonymous();

	/**
	 * Returns the set of all roles held by the security identity. These roles must be resolvable in advance for every request.
	 * <p>
	 * Note that roles are returned on a best effort basis. To actually check if
	 * a user holds a role {@link #hasRole(String)} should be used instead. Some API's (e.g. JAX-RS) do not allow
	 * for all roles to be returned, so if the underlying identity representation does not support retrieving all the roles
	 * this method will not always be reliable.
	 *
	 * <p>
	 * This set should either be unmodifiable, or a defensive copy so attempts to change the role set do not modify
	 * the underlying identity.
	 *
	 * @return The set of all roles held by the user
	 */
	Set<String> getRoles();

	/**
	 * Checks if a security identity has a given role. These roles must be resolvable in advance for every request.
	 * <p>
	 * If more advanced authorization support is required than can be provided by a simple role based system
	 * then {@link #checkPermission(Permission)} and {@link #checkPermissionBlocking(Permission)} should be used
	 * instead.
	 * <p>
	 *
	 * @return <code>true</code> if the identity has the specified role.
	 */
	boolean hasRole(String role);

	/**
	 * Gets an attribute from the identity.
	 *
	 * @param name The attribute name
	 * @param <T>  The type of the attribute
	 * @return The attribute value
	 */
	<T> T getAttribute(String name);

	/**
	 * @return All the request attributes
	 */
	Map<String, Object> getAttributes();

	/**
	 * Checks if a security identity holds a given permission.
	 * <p>
	 * This method is asynchronous, as it may involve calls to a remote resource.
	 *
	 * @param permission The permission
	 * @return CompletionStage that will resolve to true if the security identity has the specified permission
	 */
	CompletionStage<Boolean> checkPermission(Permission permission);

	/**
	 * Checks if a user holds a given permission.
	 * <p>
	 * This method is a blocking version of {@link #checkPermission(Permission)}..
	 *
	 * @param permission The permission
	 * @return true if the security identity has the specified permission
	 */
	default boolean checkPermissionBlocking(Permission permission) {
		return checkPermission(permission).toCompletableFuture().join();
	}
}
