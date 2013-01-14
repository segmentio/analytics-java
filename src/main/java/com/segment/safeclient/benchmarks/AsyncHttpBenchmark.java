package com.segment.safeclient.benchmarks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.segment.safeclient.AsyncHttpBatchedOperation;
import com.segment.safeclient.benchmark.BenchmarkResult;
import com.segment.safeclient.benchmark.ThreadedBenchmark;
import com.segment.safeclient.benchmark.ThreadedBenchmark.Operation;

public class AsyncHttpBenchmark {

	public void disableLogging() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
	}
	
	public void testRequests() {

		try {
			
			String filename = "AsyncHttpTest.csv";
			
			File file = Paths.get(filename).toFile();
			if (file.exists()) file.delete();
			
			FileWriter writer = new FileWriter(filename);
			
			int valuesToAdd = 1000000;

			int[] threadCounts = {
					2,
					10,
					20
			};
				
			int index = -1;
			for (int threads : threadCounts) {
				
				index += 1;
				
				ThreadedBenchmark benchmark = 
						new ThreadedBenchmark(
								index,
								threads, 
								valuesToAdd,
								0,
								false,
								new Operation() {
	
								public void perform() {
									
									operation.perform(RandomStringUtils.randomAlphanumeric(20));
									
								}
				});
				
				BenchmarkResult result = benchmark.run();
				
				String statistics = operation.statistics.toString()
						.replaceAll(",", "-")
						.replaceAll("\n", "  ====   ");
				
				String line = StringUtils.join(Arrays.asList(
						"" + (index+1),
						"" + result.getMin(),
						"" + result.getMax(),
						"" + result.getAverage(), 
						"" + statistics), ",");
				
				System.out.println(line);
				
				writer.write(line + "\n");
				
				// now we have to wait until the flushes finish
				operation.clear();
				
				Thread.sleep(2000);
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private AsyncHttpBatchedOperation<String> operation = 
			new AsyncHttpBatchedOperation<String>() {

		
		protected int getMaxFlushAmount() {
			return 250;
		}
		
		protected int getMaxQueueSize() {
			return 50000; 
		}
		
		@Override
		public Request buildRequest(List<String> batch) {
			
			String payload = StringUtils.join(batch, "-");
			
			return new RequestBuilder()
				.setMethod("POST")
				.setBody(payload)
				.setUrl("http://127.0.0.1:82/woo")
				.build();
		}

	};
	
}
