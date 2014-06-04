package com.github.segmentio.request;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.segmentio.AnalyticsClient;
import com.github.segmentio.Config;
import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Batch;
import com.github.segmentio.request.RetryingRequester;

@RunWith(MockitoJUnitRunner.class)
public class RetryingRequesterTest {

    private static final int HTTP_TIMEOUT = 1000;

    private Config options;
    private AnalyticsClient client;
    private RetryingRequester requester;

    private RetryingHttpServer server;
    
    @Before
    public void setup() throws IOException {
        options = new Config();
        server = new RetryingHttpServer();
        options.setTimeout(HTTP_TIMEOUT);
        options.setHost("http://localhost:" + server.getServerPort());
    	client = new AnalyticsClient("write-key", options);
        requester = new RetryingRequester(client);
    }
    
    @After
    public void teardown() throws IOException {
        server.stop();
    }

    @Test
    public void testHttpRequest() throws ClientProtocolException, IOException {
    	server.setBadResponses(options.getRetries());
        Assert.assertTrue(requester.send(new Batch("write-key", new ArrayList<BasePayload>())));
    }
}
