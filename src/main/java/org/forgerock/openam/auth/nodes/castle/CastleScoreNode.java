package org.forgerock.openam.auth.nodes.castle;

import static org.forgerock.openam.auth.nodes.castle.CastleHelper.RISK;
import static org.forgerock.openam.auth.nodes.castle.CastleHelper.getRiskResponse;

import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.InputState;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.OutcomeProvider;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.auth.nodes.validators.DecimalBetweenZeroAndOneValidator;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.sm.RequiredValueValidator;

@Node.Metadata(outcomeProvider = CastleScoreNode.CastleScoreOutcomeProvider.class,
        configClass = CastleScoreNode.Config.class, tags = {"risk"})
public class CastleScoreNode implements Node {

    private static final String BUNDLE = "org/forgerock/openam/auth/nodes/castle/CastleScoreNode";
    private final Config config;
    private final Logger logger = LoggerFactory.getLogger("amAuth");


    /**
     * Configuration for the node.
     */
    public interface Config {

        /**
         * Policy Score Threshold
         */
        @Attribute(order = 100, validators = {RequiredValueValidator.class, DecimalBetweenZeroAndOneValidator.class})
        default String scoreThreshold() {
            return ".6";
        }

    }

    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     */
    @Inject
    public CastleScoreNode(@Assisted Config config) {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("Starting Castle Score Node");
        JsonValue riskResponse = getRiskResponse(context);
        double policyScore = riskResponse.get(RISK).asDouble();
        if (policyScore >= Double.parseDouble(config.scoreThreshold())) {
            return Action.goTo(CastleScoreOutcome.GREATER_THAN_OR_EQUAL.name()).build();
        }
        return Action.goTo(CastleScoreOutcome.LESS_THAN.name()).build();

    }

    @Override
    public InputState[] getInputs() {
        return new InputState[]{new InputState(CastleHelper.CASTLE_RESPONSE, true)};
    }


    /**
     * The possible outcomes for the Castle Score Node.
     */
    private enum CastleScoreOutcome {
        GREATER_THAN_OR_EQUAL,
        LESS_THAN
    }

    /**
     * Defines the possible outcomes from this Castle Score Node
     */
    public static class CastleScoreOutcomeProvider implements OutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE,
                                                                       CastleScoreNode.class
                                                                               .getClassLoader());
            return ImmutableList.of(
                    new Outcome(CastleScoreOutcome.GREATER_THAN_OR_EQUAL.name(),
                                bundle.getString("greaterThanOrEqualOutcome")),
                    new Outcome(CastleScoreOutcome.LESS_THAN.name(), bundle.getString("lessThanOutcome")));
        }
    }
}
