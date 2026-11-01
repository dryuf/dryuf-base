package net.dryuf.base.locale;

import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;


public class BasicLocaleContextTest
{
	private static final Currency CZK = Currency.getInstance("CZK");

	private final BasicLocaleContext context = BasicLocaleContext.of(
		Locale.forLanguageTag("cs-CZ"), ZoneId.of("Europe/Prague"), CZK, "retail");

	@Test
	public void accessors_values_returned()
	{
		assertEquals(context.locale(), Locale.forLanguageTag("cs-CZ"));
		assertEquals(context.timezone(), ZoneId.of("Europe/Prague"));
		assertEquals(context.currency(), CZK);
		assertEquals(context.domain(), "retail");
	}

	@Test
	public void attributes_all_returned()
	{
		assertEquals(context.attributes(), Map.of(
			"locale", "cs-CZ",
			"timezone", "Europe/Prague",
			"currency", "CZK",
			"domain", "retail"
		));
	}

	@Test
	public void readAttribute_known_returned()
	{
		assertEquals(context.readAttribute("currency"), Optional.of("CZK"));
		assertEquals(context.readAttribute("domain"), Optional.of("retail"));
	}

	@Test
	public void readAttribute_unknown_empty()
	{
		assertEquals(context.readAttribute("other"), Optional.empty());
	}

	@Test
	public void builder_missingField_throws()
	{
		expectThrows(NullPointerException.class, () -> BasicLocaleContext.builder()
			.locale(Locale.ROOT).timezone(ZoneId.of("UTC")).currency(CZK).build());
	}
}
