package com.github.segmentio.test;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.segmentio.models.Props;
import com.github.segmentio.utils.GSONUtils;
import com.google.gson.Gson;

public class SerializationTest {

	private static Gson gson;
	
	@BeforeClass
	public static void setup() {
		gson = GSONUtils.BUILDER.create();
	}

	@Test
	public void testArrays() {
		
		String[] stringArray = { "One", "Two" };
		
		Props props = new Props(
			"string", "Some string",
			"stringArray", stringArray
		);
		
		String json = gson.toJson(props);
		
		Assert.assertEquals("{\"stringArray\":[\"One\",\"Two\"],\"string\":\"Some string\"}", json);
	}

	@Test
	public void testLists() {
		
		List<String> list = new LinkedList<String>();
		list.add("One");
		list.add("Two");
		
		Props props = new Props(
			"string", "Some string",
			"stringList", list
		);
		
		String json = gson.toJson(props);
		
		Assert.assertEquals("{\"stringList\":[\"One\",\"Two\"],\"string\":\"Some string\"}", json);
	}
	

	@Test
	public void testBigDecimal() {
		
		BigDecimal decimal = BigDecimal.valueOf(1.202);
		
		Props props = new Props(
			"string", "Some string",
			"decimal", decimal
		);
		
		String json = gson.toJson(props);
		
		Assert.assertEquals("{\"decimal\":1.202,\"string\":\"Some string\"}", json);
	}

}
