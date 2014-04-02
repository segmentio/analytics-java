package com.github.segmentio.request;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class RetryingHttpServer {

    private Server server;
    private Connection connection;
    private RequestHandler requestHandler;
    
    private String responseText = "{\"success\":true}";
    private int badResponses = 0;
    private int serverPort = 8081;
    
    public class RequestHandler implements Container {
        public void handle(Request request, Response response) {
            try {
                PrintStream body = response.getPrintStream();
                long time = System.currentTimeMillis();
    
                response.setValue("Content-Type", "application/json");
                response.setValue("Server", "StubHttpServer/1.0 (Simple 4.0)");
                response.setDate("Date", time);
                response.setDate("Last-Modified", time);
                
                if (badResponses > 0) {
                	response.setCode(500);
                	badResponses -= 1;
                } else {
                	response.setCode(200);
                }
                
                body.println(responseText);
                body.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public RetryingHttpServer() throws IOException {
        requestHandler = new RequestHandler();
        
        server = new ContainerServer(requestHandler);
        connection = new SocketConnection(server);

        SocketAddress address = new InetSocketAddress(serverPort);
        connection.connect(address);
    }

    public void stop() throws IOException {
        connection.close();
        server.stop();
    }
    
    public void setBadResponses(int r) {
        badResponses = r;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
}
