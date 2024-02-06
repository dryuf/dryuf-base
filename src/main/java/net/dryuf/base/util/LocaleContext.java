package net.dryuf.base.util;

import java.time.ZoneId;
import java.util.Locale;


/**
 * Representation of client localization.
 *
 * The instances are supposed to be immutable.
 */
public interface LocaleContext
{
       /**
        * Locale.
        *
        * @return
        *      the locale.
        */
       Locale locale();

       /**
        * TimeZone.
        *
        * @return
        *      the timezone.
        */
       ZoneId timeZone();
}
