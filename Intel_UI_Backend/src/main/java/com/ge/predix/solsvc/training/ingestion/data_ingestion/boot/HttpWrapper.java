package com.ge.predix.solsvc.training.ingestion.data_ingestion.boot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;


public class HttpWrapper {

	//Read from Properties file
    private final static String   RESTSERVER = "http://cargo-life.run.aws-usw02-pr.ice.predix.io/addShelfLife"; 
    
	public static String postHttpResource( String entity) throws ClientProtocolException, IOException {
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost(RESTSERVER);

		HttpEntity httpEntity = new StringEntity(entity);
		
		postRequest.addHeader("Content-Type","application/json");
		
		postRequest.setEntity(httpEntity);
		
		HttpResponse response = client.execute(postRequest);


		System.out.println("Response Code : " 
	                + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		System.out.println(result.toString());

		return result.toString();

	}

	
	
	
}
