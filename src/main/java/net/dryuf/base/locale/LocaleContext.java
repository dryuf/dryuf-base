package net.dryuf.base.locale;

import java.time.ZoneId;
import java.util.Currency;
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
       String DEFAULT_DOMAIN = "";

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
        * Preferred currency, for presenting monetary amounts.
        *
        * Note this is client preference for display, not currency of business data (account, order, etc.).
        *
        * @return
        *      the currency.
        */
       Currency currency();

       /**
        * Business domain, identifying business context (e.g. brand, product line or tenant), which may change
        * translations and other presentation.
        *
        * Always non-null, with empty string as default value.
        *
        * @return
        *      the domain.
        */
       String domain();

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
