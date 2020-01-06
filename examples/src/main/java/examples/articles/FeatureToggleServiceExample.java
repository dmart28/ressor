package examples.articles;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.Validate;
import xyz.ressor.Ressor;
import xyz.ressor.source.git.GitRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static xyz.ressor.translator.Translators.yamlList;

public class FeatureToggleServiceExample {

    public static void main(String[] args) {
        var ressor = Ressor.create();

        var featureToggle = ressor.service(FeatureToggleService.class)
                .translator(yamlList(FeatureToggle.class))
                .source(GitRepository.remote()
                        .repositoryURI("https://github.com/dmart28/ressor-feature-toggle.git")
                        .build())
                .resource(GitRepository.path("toggles.yaml", "master"))
                .build();

        ressor.poll(featureToggle).every(5, TimeUnit.SECONDS);

        System.out.println("Is new design enabled = " + featureToggle.isEnabled("new-design"));
        System.out.println("Is items list enabled = " + featureToggle.isEnabled("items-list"));

        ressor.shutdown();
    }

    public static class FeatureToggleService {
        private final Map<String, ToggleState> states;

        public FeatureToggleService(List<FeatureToggle> toggleList) {
            this.states = toggleList.stream().collect(Collectors.toUnmodifiableMap(k -> k.featureName, v -> v.state));
        }

        public boolean isEnabled(String featureName) {
            return Validate.notNull(states.get(featureName), featureName) == ToggleState.ENABLED;
        }
    }

    public static class FeatureToggle {
        @JsonProperty("feature")
        public String featureName;
        @JsonProperty("state")
        public ToggleState state;
    }

    public enum ToggleState {
        ENABLED, DISABLED
    }

}
