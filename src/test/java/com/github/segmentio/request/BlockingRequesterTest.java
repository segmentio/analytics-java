package com.github.segmentio.request;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.segmentio.AnalyticsClient;
import com.github.segmentio.Config;
import com.github.segmentio.request.BlockingRequester;

@RunWith(MockitoJUnitRunner.class)
public class BlockingRequesterTest {

    private static final int HTTP_TIMEOUT = 1000;

    @Mock
    private AnalyticsClient client;
    private Config options = new Config();
    private BlockingRequester requester;
    private static final String RESPONSE = "Timeout Test!";

    private StubHttpServer server;
    
    @Before
    public void setup() throws IOException {
        server = new StubHttpServer();
        options.setTimeout(HTTP_TIMEOUT);
        options.setHost("http://localhost:" + server.getServerPort());
        Mockito.when(client.getOptions()).thenReturn(options);
        requester = new BlockingRequester(client);
        
    }
    
    @After
    public void teardown() throws IOException {
        server.stop();
    }

    @Test
    public void testHttpRequest() throws ClientProtocolException, IOException {
        // this should execute quickly with no timeout
        String responseText = executeHttpRequest(HTTP_TIMEOUT/2);
        Assert.assertEquals(RESPONSE, responseText);
    }   
    
    @Test(expected = SocketTimeoutException.class)
    public void testSocketTimeout() throws IOException {
        executeHttpRequest(HTTP_TIMEOUT*10);
    }   
        
    private String executeHttpRequest(int serverTimeout) throws IOException {
        // delay a good long time
        server.setResponseDelay(serverTimeout);
        server.setResponseText(RESPONSE);
        
        HttpResponse response = requester.executeRequest("write-key", "{\"key\":\"value\"");
        
        return requester.readResponseBody(response);
    }
}
