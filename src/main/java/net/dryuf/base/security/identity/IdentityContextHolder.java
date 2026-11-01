package net.dryuf.base.security.identity;


/**
 * Holder providing IdentityContext.
 *
 * Use this interface only at highest layers to obtain IdentityContext, to avoid clashes with multithreaded execution.
 */
public interface IdentityContextHolder
{
	public IdentityContext getIdentityContext();
}
