package org.forgerock.openam.auth.nodes.castle;


import static java.util.Collections.emptyList;
import static org.forgerock.openam.auth.node.api.Action.goTo;
import static org.forgerock.openam.auth.nodes.castle.CastleHelper.NONE_TRIGGERED;
import static org.forgerock.openam.auth.nodes.castle.CastleHelper.SIGNALS;
import static org.forgerock.openam.auth.nodes.castle.CastleHelper.getRiskResponse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.json.JsonValueException;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.InputState;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.OutcomeProvider;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

@Node.Metadata(outcomeProvider = CastleSignalNode.CastleSignalOutcomeProvider.class,
        configClass = CastleSignalNode.Config.class, tags = {"risk"})
public class CastleSignalNode implements Node {

    private final Config config;
    private final Logger logger = LoggerFactory.getLogger("amAuth");


    /**
     * Configuration for the node.
     */
    public interface Config {

        /**
         * The list of possible outcomes.
         *
         * @return The possible outcomes.
         */
        @Attribute(order = 100)
        List<String> signalOutcomes();

    }

    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     */
    @Inject
    public CastleSignalNode(@Assisted Config config) {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("Starting Castle Signal Node");
        JsonValue riskResponse = getRiskResponse(context);
        Set<String> reasonCodes = riskResponse.get(SIGNALS).asMap().keySet();

        if (CollectionUtils.isEmpty(reasonCodes)) {
            return goTo(NONE_TRIGGERED).build();
        }
        List<String> outcomes = config.signalOutcomes();
        for (String outcome : outcomes) {
            if (reasonCodes.contains(outcome)) {
                return goTo(outcome).build();
            }
        }
        return goTo(NONE_TRIGGERED).build();
    }

    @Override
    public InputState[] getInputs() {
        return new InputState[]{new InputState(CastleHelper.CASTLE_RESPONSE, true)};
    }

    /**
     * Defines the possible outcomes from this Castle Signal Node.
     */
    public static class CastleSignalOutcomeProvider implements OutcomeProvider {

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            try {
                List<Outcome> outcomes = nodeAttributes.get("signalOutcomes").required()
                                                       .asList(String.class)
                                                       .stream()
                                                       .map(outcome -> new Outcome(outcome, outcome))
                                                       .collect(Collectors.toList());
                outcomes.add(new Outcome(NONE_TRIGGERED, NONE_TRIGGERED));
                return outcomes;
            } catch (JsonValueException e) {
                return emptyList();
            }
        }
    }
}
