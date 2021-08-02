package org.forgerock.openam.auth.nodes.castle;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;

public class CastleHelper {
    static final String APP_ID = "app_id";
    static final String REQUEST_TOKEN = "request_token";
    static final String CASTLE_RESPONSE = "castle_response";
    static final String POLICY = "policy";
    static final String ACTION = "action";
    static final String RISK = "risk";
    static final String SIGNALS = "signals";
    static final String NONE_TRIGGERED = "None Triggered";
    static final String DEVICE = "device";
    static final String TOKEN = "token";
    static final String SCRIPT = "var script = document.createElement('script');\n" +
            "script.type = 'text/javascript';\n" +
            "script.src = '%1$s'\n" +
            "document.getElementsByTagName('head')[0].appendChild(script);\n" +
            "var submitCollectedData = function functionSubmitCollectedData() {\n" +
            "_castle('createRequestToken').then(function(requestToken) {" +
            "loginHelpers.setHiddenCallback('request_token', requestToken)})}\n" +
            "if (typeof loginHelpers !== 'undefined') {\n" +
            "    loginHelpers.nextStepCallback(submitCollectedData)\n" +
            "} else {\n" +
            "var submitCollectedDataXUI = function functionSubmitCollectedData() {\n" +
            "_castle('createRequestToken').then(function(requestToken) {\n" +
            "document.getElementById('request_token').value = requestToken;\n" +
            "document.getElementById('loginButton_0').click();})};\n" +
            "var submitButton = document.getElementsByClassName('btn-primary')[0];\n" +
            "submitButton.addEventListener('click', submitCollectedDataXUI, false);}\n";

    static JsonValue getRiskResponse(TreeContext context) throws NodeProcessException {
        if (!context.sharedState.isDefined(CASTLE_RESPONSE)) {
            throw new NodeProcessException("Unable to find Castle" + CASTLE_RESPONSE +
                                                   " in sharedState. Does the Castle Risk node precede" +
                                                   " this node and return a successful response?");
        }
        return context.sharedState.get(CASTLE_RESPONSE);
    }
}
