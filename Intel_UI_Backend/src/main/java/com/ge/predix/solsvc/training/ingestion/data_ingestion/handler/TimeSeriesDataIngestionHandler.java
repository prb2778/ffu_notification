package com.ge.predix.solsvc.training.ingestion.data_ingestion.handler;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;


import org.apache.http.Header;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Component;

import com.ge.predix.solsvc.bootstrap.tsb.client.TimeseriesWSConfig;
import com.ge.predix.solsvc.bootstrap.tsb.factories.TimeseriesFactory;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.AdresseeDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.FanParamsDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.IngestDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.ParamsDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.SimpleResponseDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.websocket.WebSocketClient;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.websocket.WebSocketConfig;
import com.ge.predix.timeseries.client.ClientFactory;
import com.ge.predix.timeseries.client.TenantContext;
import com.ge.predix.timeseries.client.TenantContextFactory;
import com.ge.predix.timeseries.model.builder.IngestionRequestBuilder;
import com.ge.predix.timeseries.model.builder.IngestionTag;
import com.ge.predix.timeseries.model.datapoints.DataPoint;
import com.ge.predix.timeseries.model.datapoints.Quality;
import com.ge.predix.timeseries.model.response.IngestionResponse;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * 
 * @author predix -
 */
@Component
public class TimeSeriesDataIngestionHandler extends BaseFactoryIT
{
    private static Logger  log = Logger.getLogger(TimeSeriesDataIngestionHandler.class);
    
    private static final String pzid = "Predix-Zone-Id";
    @Autowired
    private TimeseriesFactory timeSeriesFactory;

    @Autowired
    private AssetDataHandler  assetDataHandler;
    
    
	@Autowired
	private TimeseriesWSConfig tsInjectionWSConfig;

	@Autowired
	private WebSocketConfig wsConfig;
	
	@Autowired
	private WebSocketClient wsClient;
    

   

	/**
	 * @param params -
	 */
	public String ingestTS(List<ParamsDTO> params) {
		try
        {
			String	authorization = null;
			List<Header> lHead = restClient.getSecureTokenForClientId();
        	for (Header header : lHead) {
        		authorization = header.getValue();
			}
        	TenantContext tenant = TenantContextFactory.createIngestionTenantContextFromProvidedProperties
        			(this.tsInjectionWSConfig.getInjectionUri(), authorization,this.pzid,this.tsInjectionWSConfig.getZoneId());
        	String key = "Key";
        	IngestionRequestBuilder ingestionBuilder = null;
        	IngestionResponse response = null;
        	ingestionBuilder = IngestionRequestBuilder.createIngestionRequest().withMessageId("messageid");
            	for (ParamsDTO dataValue : params) {
            	
            		if(dataValue.getQuality()==3)
            	ingestionBuilder.addIngestionTag(IngestionTag.Builder.createIngestionTag()
                .withTagName(dataValue.getTagId())
                .addDataPoints(Arrays.asList(new DataPoint(new Long(dataValue.getTimeStamp()), dataValue.getValue(), Quality.GOOD))
                ).addAttribute("key", key).build());
            		else if (dataValue.getQuality()==0)
            			ingestionBuilder.addIngestionTag(IngestionTag.Builder.createIngestionTag()
            	                .withTagName(dataValue.getTagId())
            	                .addDataPoints(Arrays.asList(new DataPoint(new Long(dataValue.getTimeStamp()), dataValue.getValue(), Quality.BAD))
            	                ).addAttribute("key", key).build());
            		else if (dataValue.getQuality()==2)
            			ingestionBuilder.addIngestionTag(IngestionTag.Builder.createIngestionTag()
            	                .withTagName(dataValue.getTagId())
            	                .addDataPoints(Arrays.asList(new DataPoint(new Long(dataValue.getTimeStamp()), dataValue.getValue(), Quality.NOT_APPLICABLE))
            	                ).addAttribute("key", key).build());
            		else if (dataValue.getQuality()==1)
            			ingestionBuilder.addIngestionTag(IngestionTag.Builder.createIngestionTag()
            	                .withTagName(dataValue.getTagId())
            	                .addDataPoints(Arrays.asList(new DataPoint(new Long(dataValue.getTimeStamp()), dataValue.getValue(), Quality.UNCERTAIN))
            	                ).addAttribute("key", key).build());
            		else 
            			ingestionBuilder.addIngestionTag(IngestionTag.Builder.createIngestionTag()
            	                .withTagName(dataValue.getTagId())
            	                .addDataPoints(Arrays.asList(new DataPoint(new Long(dataValue.getTimeStamp()), dataValue.getValue(), Quality.GOOD))
            	                ).addAttribute("key", key).build());
                
                response = ClientFactory.ingestionClientForTenant(tenant).ingestAll(ingestionBuilder.build());
                ingestionBuilder = IngestionRequestBuilder.createIngestionRequest().withMessageId("messageid");
                }
            ingestionBuilder = null;
        	response = null;
        	authorization = null;
        	key = null;
       } catch (Exception e) {
        	log.info("ERROR:    "+e.getMessage());
			e.printStackTrace();
			return "ERROR:    "+e.getMessage();
		}
       finally{
    	   log.info("FINALLY >>>>>>>>>>>>>>> : ");
    	   System.gc();
    	   Runtime.getRuntime().gc();
       }
		return " Datapoints were ingested  ";
		
	}




	/**
	 * @param ingestList
	 * @return -
	 */
	public void ingestFilesIntoTS(List<IngestDTO> ingestList) {
		try
        {
			String	authorization = null;
			
        	List<Header> lHead = restClient.getSecureTokenForClientId();
        	for (Header header : lHead) {
        		authorization = header.getValue();
			}
        	TenantContext tenant = TenantContextFactory.createIngestionTenantContextFromProvidedProperties
        			(this.tsInjectionWSConfig.getInjectionUri(), authorization,this.pzid,this.tsInjectionWSConfig.getZoneId());
        	
        	String key = "Key";
        	IngestionRequestBuilder ingestionBuilder = null;
        	IngestionResponse response = null;
        	
            ingestionBuilder = IngestionRequestBuilder.createIngestionRequest().withMessageId("messageid");
            	int count = 0;
            	for (IngestDTO dataValue : ingestList) {
                ingestionBuilder.addIngestionTag(IngestionTag.Builder.createIngestionTag()
                .withTagName(dataValue.getTagId())
                .addDataPoints(Arrays.asList(new DataPoint(dataValue.getTimestamp().getTime(), dataValue.getValue(), Quality.GOOD))
                ).addAttribute("key", key).build());
                
                if(count==1000){
                	response = ClientFactory.ingestionClientForTenant(tenant).ingestAll(ingestionBuilder.build());
                	count=0;
                	ingestionBuilder = IngestionRequestBuilder.createIngestionRequest().withMessageId("messageid");
                }
                else
                	count++;

                }
            System.out.println("<<<<<<<<<< Datapoints were ingested >>>>>>>>>>");
            ingestionBuilder = null;
        	response = null;
        	authorization = null;
        	key = null;
        	
		} catch (Exception e) {
        	log.info("ERROR >>>>>>>>>>>>>>> : "+e.getMessage());
			e.printStackTrace();
		}
       finally{
    	   log.info("FINALLY >>>>>>>>>>>>>>> : ");
    	   System.gc();
    	   Runtime.getRuntime().gc();
       }
		
	}




	/**
	 * @param sensor
	 * @param authorization 
	 * @return -
	 */
	public SimpleResponseDTO resetSensor(FanParamsDTO sensor, String authorization) {
		try {
//			List<Header> headers =  new ArrayList<Header>();
//			this.restClient.addSecureTokenToHeaders(headers, authorization);
////			 headers = restClient.getSecureTokenForClientId();
////        	for (Header header : headers) {
////        		authorization = header.getValue();
////			}
//			TenantContext tenant = TenantContextFactory.createIngestionTenantContextFromProvidedProperties
//        			(this.tsInjectionWSConfig.getInjectionUri(), authorization,this.pzid,this.tsInjectionWSConfig.getZoneId());
//			
//			IngestionRequestBuilder ingestionBuilder = null;
//        	IngestionResponse response = null;
//        	
//        	
//        	
//        	
//            ingestionBuilder = IngestionRequestBuilder.createIngestionRequest().withMessageId("messageid");
//            	
//            	Timestamp stmp = new Timestamp(Calendar.getInstance().getTimeInMillis());
//                ingestionBuilder.addIngestionTag(IngestionTag.Builder.createIngestionTag()
//                .withTagName(sensor.getGateway().concat("_").concat(sensor.getFan()).concat("_Soft_Fail_Detected_Reset"))
//                .addDataPoints(Arrays.asList(new DataPoint(stmp.getTime(), 1, Quality.GOOD))).addAttribute("key", "").build());
//                
//               try {
//            	   response = ClientFactory.ingestionClientForTenant(tenant).ingestAll(ingestionBuilder.build());
//			} catch (Exception e) {
//				return "Invalid Token";
//			}
//            ingestionBuilder = null;
//        	response = null;
//        	authorization = null;
        	
			Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			JedisPool redisPool = new JedisPool(new JedisPoolConfig(), prop.getProperty("redis.host"),new Integer(prop.getProperty("redis.port")), Protocol.DEFAULT_TIMEOUT, prop.getProperty("redis.pwd"));
			Jedis redis = redisPool.getResource();
			redis.set(sensor.getGateway().concat("_").concat(sensor.getFan()).concat("_HardFailDetected"),"noError");
			redis.set(sensor.getGateway().concat("_").concat(sensor.getFan()).concat("_Soft_Fail_Detected"),"noError");
			redis.set(sensor.getGateway().concat("_").concat(sensor.getFan()).concat("_Soft_Fail_Detected_emailSF"),"0");
			redis.set(sensor.getGateway().concat("_").concat(sensor.getFan()).concat("_Soft_Fail_Detected_emailHF"),"0");
			redis.close();
			SimpleResponseDTO dto = new SimpleResponseDTO();
			dto.setResponse("SUCCESS");
			return dto;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	
	}




	/**
	 * @param param
	 * @return -
	 */
	public String setAdressee(AdresseeDTO param) {
		try {
			Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			JedisPool redisPool = new JedisPool(new JedisPoolConfig(), prop.getProperty("redis.host"),new Integer(prop.getProperty("redis.port")), Protocol.DEFAULT_TIMEOUT, prop.getProperty("redis.pwd"));
			Jedis redis = null;
			redis = redisPool.getResource();
			if(param.getFailure()==0){
			redis.set("adresseeListSF",param.getEmail().toString());
			}
			else if (param.getFailure()==1){
				redis.set("adresseeListHF",param.getEmail().toString());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}
		return "SUCCESS";
	}
}
