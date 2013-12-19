package com.github.segmentio.request;

import java.io.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.segmentio.AnalyticsClient;
import com.github.segmentio.Constants;
import com.github.segmentio.models.*;
import com.github.segmentio.stats.AnalyticsStatistics;
import com.github.segmentio.utils.GSONUtils;
import com.google.gson.Gson;

public class BlockingRequester implements IRequester {

	private static final Logger logger = LoggerFactory
			.getLogger(Constants.LOGGER);

	private AnalyticsClient client;
	private Gson gson;

	private CloseableHttpClient httpClient;
	
	private RequestConfig defaultRequestConfig;

	public BlockingRequester(AnalyticsClient client) {

		this.client = client;
		
		httpClient = HttpClients.createDefault();
		
		defaultRequestConfig = 
		        RequestConfig.custom()
		        .setCookieSpec(CookieSpecs.BEST_MATCH)
		        .setExpectContinueEnabled(true)
		        .setStaleConnectionCheckEnabled(true).build();
		
		this.gson = GSONUtils.BUILDER.create();
	}

	public void send(Batch batch) {

		AnalyticsStatistics statistics = client.getStatistics();
		
		try {
			
			long start = System.currentTimeMillis();
			
			HttpPost post = buildPostRequest(batch);
			
			HttpResponse response = httpClient.execute(post);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			StringBuilder responseBuilder = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				responseBuilder.append(line);
			}
			
			String responseBody = responseBuilder.toString();
			int statusCode = response.getStatusLine().getStatusCode();
			
			long duration = System.currentTimeMillis() - start;
			
			statistics.updateRequestTime(duration);
			
			if (statusCode == 200) {

				String message = "Successful analytics request. [code = "
						+ statusCode + "]. Response = " + responseBody;
				
				logger.debug(message);
				report(statistics, batch, true, message);
				
			} else {

				
				String message = "Failed analytics response [code = " + statusCode + 
						"]. Response = " + responseBody;
				
				logger.error(message);
				report(statistics, batch, false, message);
			}

		} catch (IOException e) {
			String message = "Failed analytics response." + e.getMessage();
			logger.error(message, e);
			report(statistics, batch, false, message);
		}

	}

    public HttpPost buildPostRequest(Batch batch) throws UnsupportedEncodingException {
        
        RequestConfig requestConfig = 
                RequestConfig.copy(defaultRequestConfig)
                    .setSocketTimeout(client.getOptions().getTimeout())
                    .setConnectTimeout(client.getOptions().getTimeout())
                    .setConnectionRequestTimeout(client.getOptions().getTimeout()).build();
        
        HttpPost post =
                new HttpPost(client.getOptions().getHost() + "/v1/import");
        post.setConfig(requestConfig);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        
        String json = gson.toJson(batch);
        post.setEntity(new ByteArrayEntity(json.getBytes("UTF-8")));
        return post;
    }
	
	private void report(AnalyticsStatistics statistics, Batch batch, boolean success, String message) {
		for (BasePayload payload : batch.getBatch()) {
			Callback callback = payload.getCallback();
			
			if (success) {
				statistics.updateSuccessful(1);
			} else {
				statistics.updateFailed(1);
			}
			
			if (callback != null) {
				callback.onResponse(success, message);
			}
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
