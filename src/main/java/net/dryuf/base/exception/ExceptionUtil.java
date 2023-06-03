package net.dryuf.base.exception;

import lombok.SneakyThrows;


/**
 * Exception utilities.
 */
public class ExceptionUtil
{
    /**
     * Throws potentially checked exception without marking the method signature.
     *
     * @param ex
     *      exception to throw
     *
     * @return
     *      nothing.  Formally a {@link RuntimeException}, so this can be used in <code>throw sneakyThrow(ex);</code>.
     */
    @SneakyThrows
    public static RuntimeException sneakyThrow(Throwable ex)
    {
        throw ex;
    }
}
