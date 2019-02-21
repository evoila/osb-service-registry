package de.evoila.osb.service.registry.util;

import org.mockserver.integration.ClientAndServer;


public class MockServer {

    public static final int SHARE = 1;

    private int port;
    private ClientAndServer mockServer;

    public MockServer(int testcase, int port) {
        this.port = port;
    }

    public void startServer() {
        mockServer = ClientAndServer.startClientAndServer(port);
    }

    public void stopServer() {
        mockServer.stop();
    }

}
