package net.dryuf.base.util;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.ZoneId;
import java.util.Locale;


/**
 * Basic implementation of LocaleContext, constructed via builder.
 */
@Accessors(fluent = true)
@Value
@Builder(builderClassName = "Builder")
public class BasicLocaleContext implements LocaleContext
{
       Locale locale;

       ZoneId timeZone;
}
