package net.dryuf.base.exception;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class ExceptionUtilTest
{
    @Test
    public void sneakyThrowTest()
    {
        Assert.expectThrows(IOException.class, () -> ExceptionUtil.sneakyThrow(new IOException("hello")));
    }
}
