package net.dryuf.base.util;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;


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

       /**
        * All defined attributes.
        *
        * @return
        *      all defined attributes.
        */
       Map<String, String> attributes();

       /**
        * Reads single attribute.
        *
        * @param name
        *     name of the attribute
        * @return
        *     value of the attribute, or empty if not defined
        */
       Optional<String> readAttribute(String name);
}
