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

public class StubHttpServer {

    private Server server;
    private Connection connection;
    private RequestHandler requestHandler;
    
    private int responseDelay = 0;
    private String responseText = "StubHttpServer";
    
    public class RequestHandler implements Container {
        public void handle(Request request, Response response) {
            try {
                PrintStream body = response.getPrintStream();
                long time = System.currentTimeMillis();
    
                Thread.sleep(responseDelay);
                
                response.setValue("Content-Type", "text/plain");
                response.setValue("Server", "StubHttpServer/1.0 (Simple 4.0)");
                response.setDate("Date", time);
                response.setDate("Last-Modified", time);
    
                body.println(responseText);
                body.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public StubHttpServer() throws IOException {
        requestHandler = new RequestHandler();
        
        server = new ContainerServer(requestHandler);
        connection = new SocketConnection(server);

        SocketAddress address = new InetSocketAddress(8080);
        connection.connect(address);
    }

    public void stop() throws IOException {
        connection.close();
        server.stop();
    }
    
    public void setResponseDelay(int rd) {
        responseDelay = rd;
    }
    
    public void setResponseText(String r) {
        responseText = r;
    }
    
}
