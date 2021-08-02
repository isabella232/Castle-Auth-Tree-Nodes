package org.forgerock.openam.auth.nodes.castle;


import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.InputState;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.OutcomeProvider;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

@Node.Metadata(outcomeProvider = CastleActionNode.CastleActionOutcomeProvider.class,
        configClass = CastleActionNode.Config.class, tags = {"risk"})
public class CastleActionNode implements Node {

    private static final String BUNDLE = "org/forgerock/openam/auth/nodes/castle/CastleActionNode";
    private final Logger logger = LoggerFactory.getLogger("amAuth");

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("Starting Castle Action Node");
        JsonValue riskResponse = CastleHelper.getRiskResponse(context);
        String action = riskResponse.get(CastleHelper.POLICY).get(CastleHelper.ACTION).asString();
        if (StringUtils.isEmpty(action)) {
            throw new NodeProcessException(
                    "Unable to find " + CastleHelper.ACTION + " in " + CastleHelper.CASTLE_RESPONSE);
        }
        if (StringUtils.equals(CastleActionOutcome.ALLOW.toString(), action)) {
            return Action.goTo(CastleActionOutcome.ALLOW.name()).build();
        } else if (StringUtils.equals(CastleActionOutcome.CHALLENGE.toString(), action)) {
            return Action.goTo(CastleActionOutcome.CHALLENGE.name()).build();
        }
        return Action.goTo(CastleActionOutcome.DENY.name()).build();
    }

    @Override
    public InputState[] getInputs() {
        return new InputState[]{new InputState(CastleHelper.CASTLE_RESPONSE, true)};
    }

    /**
     * The possible actions for the CastleActionNode
     */
    private enum CastleActionOutcome {

        ALLOW("allow"),
        CHALLENGE("challenge"),
        DENY("deny");

        private final String stringName;

        CastleActionOutcome(String stringName) {
            this.stringName = stringName;
        }

        @Override
        public String toString() {
            return stringName;
        }
    }

    /**
     * Defines the possible outcomes from this Castle Action Node.
     */
    public static class CastleActionOutcomeProvider implements OutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE,
                                                                       CastleActionNode.class
                                                                               .getClassLoader());
            return ImmutableList.of(
                    new Outcome(CastleActionOutcome.ALLOW.name(), bundle.getString("allowOutcome")),
                    new Outcome(CastleActionOutcome.CHALLENGE.name(), bundle.getString("challengeOutcome")),
                    new Outcome(CastleActionOutcome.DENY.name(), bundle.getString("denyOutcome")));
        }
    }
}
