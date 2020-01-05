package examples.articles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.tuple.Pair;
import xyz.ressor.Ressor;
import xyz.ressor.commons.annotations.ServiceFactory;
import xyz.ressor.source.http.Http;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static xyz.ressor.translator.Translators.json;

public class ExchangeRateProviderExample {

    public static void main(String[] args) {
        Ressor ressor = Ressor.create();

        ExchangeRateProvider rateService = ressor.service(ExchangeRateProvider.class)
                .source(Http.source())
                .resource(Http.url("https://api.exchangeratesapi.io/latest"))
                .translator(json(ExchangeRateData.class))
                .build();

        System.out.println("USD/IDR exchange rate: " + rateService.getRate(Currency.getInstance("USD"), Currency.getInstance("IDR")));

        ressor.shutdown();
    }

    public static class ExchangeRateProvider {
        private final Map<Pair<Currency, Currency>, Double> rates;

        public ExchangeRateProvider(Map<Pair<Currency, Currency>, Double> rates) {
            this.rates = rates;
        }

        @ServiceFactory
        public static ExchangeRateProvider create(ExchangeRateData data) {
            return new ExchangeRateProvider(buildConversionMap(data));
        }

        public double getRate(Currency from, Currency to) {
            return rates.get(Pair.of(from, to));
        }

        private static Map<Pair<Currency, Currency>, Double> buildConversionMap(ExchangeRateData data) {
            Map<Pair<Currency, Currency>, Double> result = new HashMap<>();

            data.currencyRates.forEach((currency, rate) -> {
                result.put(Pair.of(data.baseCurrency, currency), rate);
                result.put(Pair.of(currency, data.baseCurrency), 1d / rate);

                data.currencyRates.forEach((c, r) -> result.put(Pair.of(currency, c), r / rate));
            });

            return unmodifiableMap(result);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExchangeRateData {
        @JsonProperty("base")
        public Currency baseCurrency;
        @JsonProperty("rates")
        public Map<Currency, Double> currencyRates;
    }


}
