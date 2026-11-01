package net.dryuf.base.locale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.ZoneId;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;


/**
 * Basic implementation of LocaleContext, constructed via builder.
 */
@Accessors(fluent = true)
@Value
@Builder(builderClassName = "Builder")
@AllArgsConstructor(staticName = "of")
public class BasicLocaleContext implements LocaleContext
{
       @NonNull
       private Locale locale;

       @NonNull
       private ZoneId timeZone;

       @NonNull
       private Currency currency;

       @NonNull
       private String domain;

       public BasicLocaleContext of(@NonNull Locale locale, @NonNull ZoneId timeZone, @NonNull Currency currency) {
              return of(locale, timeZone, currency, DEFAULT_DOMAIN);
       }

       @Override
       public Map<String, String> attributes()
       {
              return Map.of(
                     "locale", locale.toLanguageTag(),
                     "timeZone", timeZone.getId(),
                     "currency", currency.getCurrencyCode(),
                     "domain", domain
              );
       }

       @Override
       public Optional<String> readAttribute(String name)
       {
              switch (name) {
              case "locale":
                      return Optional.of(locale.toLanguageTag());
              case "timeZone":
                      return Optional.of(timeZone.getId());
              case "currency":
                      return Optional.of(currency.getCurrencyCode());
              case "domain":
                      return Optional.of(domain);
              default:
                      return Optional.empty();
              }
       }
}
