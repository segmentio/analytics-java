package com.github.segmentio.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.segmentio.Client;
import com.github.segmentio.Constants;
import com.github.segmentio.gson.DateTimeTypeConverter;
import com.github.segmentio.models.BasePayload;
import com.github.segmentio.models.Batch;
import com.github.segmentio.models.Callback;
import com.github.segmentio.stats.AnalyticsStatistics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BlockingRequester implements IRequester {

	private static final Logger logger = LoggerFactory
			.getLogger(Constants.LOGGER);

	private Client client;
	private Gson gson;

	private HttpClient httpClient;

	public BlockingRequester(Client client) {

		this.client = client;

		final HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, client.getOptions().getTimeout());
		this.httpClient = new DefaultHttpClient(httpParams);
		
		this.gson = new GsonBuilder().registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter()).create();
	}

	public void send(Batch batch) {

		AnalyticsStatistics statistics = client.getStatistics();
		
		String json = gson.toJson(batch);

		try {
			
			long start = System.currentTimeMillis();
			
			HttpPost post = new HttpPost(client.getOptions().getHost()
					+ "/v1/import");
			post.addHeader("Content-Type", "application/json; charset=utf-8");
			post.setEntity(new ByteArrayEntity(json.getBytes("UTF-8")));
			
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
		httpClient.getConnectionManager().shutdown();
	}

}
