package com.github.segmentio.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.segmentio.AnalyticsClient;
import com.github.segmentio.Constants;
import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Batch;
import com.github.segmentio.stats.AnalyticsStatistics;
import com.github.segmentio.utils.GSONUtils;
import com.google.gson.Gson;

public class BlockingRequester implements IRequester {

	private static final Logger logger = LoggerFactory
			.getLogger(Constants.LOGGER);

	protected AnalyticsClient client;
	private Gson gson;
	private CloseableHttpClient httpClient;
	private RequestConfig defaultRequestConfig;

	public BlockingRequester(AnalyticsClient client) {
		this.client = client;
		httpClient = HttpClients.createDefault();
		int requestTimeout = client.getOptions().getTimeout();
		
        defaultRequestConfig = 
		        RequestConfig.custom()
		        .setCookieSpec(CookieSpecs.BEST_MATCH)
		        .setExpectContinueEnabled(true)
		        .setStaleConnectionCheckEnabled(true)
                .setSocketTimeout(requestTimeout)
                .setConnectTimeout(requestTimeout)
                .setConnectionRequestTimeout(requestTimeout)
                .setProxy(client.getOptions().getProxy()).build();
		
		this.gson = GSONUtils.BUILDER.create();
	}

	public boolean send(Batch batch) {
		AnalyticsStatistics statistics = client.getStatistics();
		try {
			long start = System.currentTimeMillis();
			
			// mark that the event is getting sent now
			batch.setSentAt(DateTime.now());
			
			String json = gson.toJson(batch);
			
			HttpResponse response = executeRequest(batch.getWriteKey(), json);
			String responseBody = readResponseBody(response);
			int statusCode = response.getStatusLine().getStatusCode();
			
			long duration = System.currentTimeMillis() - start;
			statistics.updateRequestTime(duration);
			
			if (statusCode == 200) {
				logger.debug("Successful analytics request. [code = {}]. Response = {}", statusCode, responseBody);
				succeed(batch, statistics);
				return true;
			} else {
				logger.error("Failed analytics response [code = {}]. Response = {}", statusCode, responseBody);
				fail(batch, statistics);
			}
		} catch (IOException e) {
			logger.error("Failed analytics response. [error = {}]", e.getMessage());
			fail(batch, statistics);
		}
		
		return false;
	}

    public HttpResponse executeRequest(String writeKey, String json) 
    		throws ClientProtocolException, IOException {  
    	
        HttpPost post =
                new HttpPost(client.getOptions().getHost() + "/v1/import");
        post.setConfig(defaultRequestConfig);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
       
		// Basic Authentication
		// https://segment.io/docs/tracking-api/reference/#authentication
		post.addHeader("Authorization", 
				"Basic " + Base64.encodeBase64((writeKey+":").getBytes()));
        
        post.setEntity(new ByteArrayEntity(json.getBytes("UTF-8")));
        
        if (logger.isTraceEnabled()) {
            logger.trace("Posting analytics data");
        }
        
        return httpClient.execute(post);
    }
	
    public String readResponseBody(HttpResponse response) throws IOException {
        BufferedReader rd 
            = new BufferedReader(
                new InputStreamReader(
                        response.getEntity().getContent()));
        
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
        	responseBuilder.append(line);
        }
        
        String responseBody = responseBuilder.toString();
        return responseBody;
    }
	
	private void succeed(Batch batch, AnalyticsStatistics statistics) {
		for (BasePayload payload : batch.getBatch()) {
			statistics.updateSuccessful(1);
		}
	}
	
	private void fail(Batch batch, AnalyticsStatistics statistics) {
		for (BasePayload payload : batch.getBatch()) {
			statistics.updateFailed(1);
		}
	}
	
	public void close() {
		try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("Error while closing", e);
        }
	}


}
