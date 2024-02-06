package net.dryuf.base.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.ZoneId;
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
       private Locale locale;

       private ZoneId timeZone;

       @Override
       public Map<String, String> attributes()
       {
              return Map.of("locale", locale.toString(), "timeZone", timeZone.toString());
       }

       @Override
       public Optional<String> readAttribute(String name)
       {
              switch (name) {
              case "locale":
                      return Optional.of(locale.toString());
              case "timeZone":
                      return Optional.of(timeZone.toString());
              default:
                      return Optional.empty();
              }
       }
}
