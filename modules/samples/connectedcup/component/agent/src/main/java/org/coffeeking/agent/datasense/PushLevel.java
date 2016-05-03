package org.coffeeking.agent.datasense;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coffeeking.agent.transport.TransportHandlerException;
import org.coffeeking.agent.transport.mqtt.ConnectedCupMQttTransportHandler;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StreamCorruptedException;

public class PushLevel extends HttpServlet {
    private static final Log log = LogFactory.getLog(PushLevel.class);
    private ConnectedCupMQttTransportHandler connectedCupMQttTransportHandler;

    public PushLevel() {
        connectedCupMQttTransportHandler = ConnectedCupMQttTransportHandler.getInstance();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String deviceId = req.getParameter("deviceId");
        String tenantDomain = req.getParameter("tenantDomain");
        String deviceOwner = req.getParameter("deviceOwner");
        String payload = req.getParameter("payload");
        payload = " {\"event\": {\"metaData\": {\"owner\": \"" + deviceOwner +
                "\", \"type\": \"coffeelevel\",\"deviceId\": " +
                "\"" + deviceId + "\",\"timestamp\": " + System.currentTimeMillis() +
                "},\"payloadData\": { \"coffeelevel\": " + Float.parseFloat(payload) + ", \"temperature\": 0} }}";
        String token = (String) req.getSession().getAttribute("token");
        if (!connectedCupMQttTransportHandler.isConnected()) {
            connectedCupMQttTransportHandler.setToken(token);
            connectedCupMQttTransportHandler.connect();
        }
        try {
            if (connectedCupMQttTransportHandler.isConnected()) {
                connectedCupMQttTransportHandler.publishToConnectedCup(deviceOwner, deviceId, payload, tenantDomain, 0,
                                                                       true);
            }
        } catch (TransportHandlerException e) {
            log.error(e);
            resp.sendError(500);
        }
    }
}
