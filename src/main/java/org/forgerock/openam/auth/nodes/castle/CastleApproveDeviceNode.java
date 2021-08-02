/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2018 ForgeRock AS.
 */


package org.forgerock.openam.auth.nodes.castle;

import static org.forgerock.openam.auth.nodes.castle.CastleHelper.CASTLE_RESPONSE;
import static org.forgerock.openam.auth.nodes.castle.CastleHelper.DEVICE;
import static org.forgerock.openam.auth.nodes.castle.CastleHelper.TOKEN;

import javax.inject.Inject;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.InputState;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.sm.annotations.adapters.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

import io.castle.client.Castle;
import io.castle.client.model.CastleSdkConfigurationException;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class,
        configClass = CastleApproveDeviceNode.Config.class, tags = {"risk"})
public class CastleApproveDeviceNode extends SingleOutcomeNode {

    private final Logger logger = LoggerFactory.getLogger("amAuth");
    private final Castle castle;


    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * The Castle API Secret
         */
        @Attribute(order = 100)
        @Password
        char[] apiSecret();

        /**
         * Base URL
         */
        @Attribute(order = 200)
        default String baseURL() {
            return "https://api.castle.io/";
        }


    }


    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     */
    @Inject
    public CastleApproveDeviceNode(@Assisted Config config) throws NodeProcessException {
        try {
            castle = Castle.initialize(Castle.configurationBuilder().apiSecret(String.valueOf(config.apiSecret()))
                                             .withApiBaseUrl(config.baseURL()).build());
        } catch (CastleSdkConfigurationException e) {
            throw new NodeProcessException("Cannot initialize the castle SDK");
        }
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("Starting Castle Device Approve Node");
        JsonValue sharedState = context.sharedState;
        String deviceToken;
        try {
            deviceToken = sharedState.get(CASTLE_RESPONSE).get(DEVICE).get(TOKEN).asString();
        } catch (Exception e) {
            throw new NodeProcessException("Unable to get device token from sharedState", e);
        }
        logger.debug("Attempting to approve device with token: {}", deviceToken);
        castle.client().approve(deviceToken);
        return goToNext().build();
    }


    @Override
    public InputState[] getInputs() {
        return new InputState[]{new InputState(CASTLE_RESPONSE, true)};
    }
}
