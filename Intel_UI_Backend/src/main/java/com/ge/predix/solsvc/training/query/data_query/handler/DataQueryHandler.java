package com.ge.predix.solsvc.training.query.data_query.handler;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;


import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.predix.solsvc.bootstrap.tsb.client.TimeseriesWSConfig;
import com.ge.predix.solsvc.restclient.impl.CxfAwareRestClient;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.DatapointDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.FailuresStsDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.FanParamsDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.HFailuresDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.QueryParamsDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.ResponseDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.SFailuresDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.SensorDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.TagStatusDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.TagsDTO;
import com.ge.predix.timeseries.client.ClientFactory;
import com.ge.predix.timeseries.client.TenantContext;
import com.ge.predix.timeseries.client.TenantContextFactory;
import com.ge.predix.timeseries.model.builder.QueryBuilder;
import com.ge.predix.timeseries.model.builder.QueryTag;
import com.ge.predix.timeseries.model.datapoints.DataPoint;
import com.ge.predix.timeseries.model.response.QueryResponse;
import com.ge.predix.timeseries.model.response.Result;
import com.ge.predix.timeseries.model.response.TagResponse;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;



/**
 * 
 * @author predix -
 */
@Component
public class DataQueryHandler
{
    private static Logger log = Logger.getLogger(DataHandler.class);
    
    private static final String PZID = "Predix-Zone-Id";
    
    private static final String[] TAGS = {"Soft_Fail_Detected","Differential_Energy_Reference","Differential_Energy","Alarm_Level","DE_Alarm_Level",
    										"X_Acceleration_Avg","Y_Acceleration_Avg","Z_Acceleration_Avg"};
   
    private static final String[] HARDTAGS = {"HardFailDetected","AvgResult","MeanResult","StdDevResult","HeartBeat_Fault"};
    
    @Autowired
	protected CxfAwareRestClient restClient;
    
    @Autowired
	private TimeseriesWSConfig tsInjectionWSConfig;
    
    @Autowired
    private TimeSeriesDataQueryHandler timeSeriesDataQueryHandler;
    /**
     *  -
     */
    @SuppressWarnings("nls")
    @PostConstruct
    public void intilizeDataIngestionHandler()
    {
        log.info("*******************DataIngestionHandler Initialization complete*********************");
    }
	/**
	 * @param params
	 * @return -
	 */
	public List<ResponseDTO> queryTSByInterval(QueryParamsDTO params) {
		try {
			return	this.timeSeriesDataQueryHandler.queryTSByInterval(params);
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * @param params
	 * @return -
	 */
	public List<ResponseDTO> queryTSOrdered(QueryParamsDTO params) {
		try {
			return	this.timeSeriesDataQueryHandler.queryTSOrdered(params);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	public void startTSMonitor() {
		 try {
			 	Properties prop = new Properties();
				String propFileName = "config.properties";
				InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
				if (inputStream != null) {
					prop.load(inputStream);
				} else {
					throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
				}
					TreeMap<String, String> mapSF = new TreeMap<>();
					TreeMap<String, String> mapHF = new TreeMap<>();
					for (String tags : this.getTags().getResults()) {
						if(tags.contains(prop.getProperty("filterTag.HFD"))){
							mapHF.put(tags, tags);
						}else if (tags.contains(prop.getProperty("filterTag.HBF"))){
							mapHF.put(tags, tags);
						}else if (tags.contains(prop.getProperty("filterTag.HB"))){
							mapHF.put(tags, tags);
						}else if (tags.contains(prop.getProperty("filterTag.SFD"))){
							mapSF.put(tags, tags);
						}
					}
					this.softFailMonitor(mapSF);
					this.hardFailMonitor(mapHF);
				} catch (Exception e) {
					e.printStackTrace();
				}
		
	}
	/**
	 *  -
	 * @param mapHF 
	 */
	private void hardFailMonitor(TreeMap<String, String> mapHF) {
		try {
//			log.info("Lets check TS");
			QueryParamsDTO params = new QueryParamsDTO();
			Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			params.setStart(new Timestamp(Calendar.getInstance().getTimeInMillis() - new Long(prop.getProperty("period.time.query"))));
			params.setEnd(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			params.setIntervalValue(0);
			List<String> tags = new ArrayList<>();
			tags.addAll(mapHF.values());
			params.setTagsArray(tags);
			
			JedisPool redisPool = new JedisPool(new JedisPoolConfig(), prop.getProperty("redis.host"),new Integer(prop.getProperty("redis.port")), Protocol.DEFAULT_TIMEOUT, prop.getProperty("redis.pwd"));
			
			String url = prop.getProperty("uaa.URI.token");
	     	   HttpClient client = HttpClientBuilder.create().build();
	     	   HttpPost post = new HttpPost(url);
	     	   post.setHeader("Authorization",prop.getProperty("uaa.basic.auth"));
	     	   post.setHeader("Content-Type","application/x-www-form-urlencoded");
	     	   post.setHeader("Accept","application/json");
	     	   
	     	   StringEntity postingString;
	     	   String body="grant_type=client_credentials";
	     	   	postingString = new StringEntity(body);
				   post.setEntity(postingString);
	     		   HttpResponse response = null;
				try {
					response = client.execute(post);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	     		   
	     		   int responseCode;
	     		   responseCode = response.getStatusLine().getStatusCode();
	     		   String result =null;
	     		   
	         		   try {
						result = EntityUtils.toString(response.getEntity());
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
	         		   
	         			   JSONObject jsonObj = new JSONObject(result);
	         			 String autorization ="Bearer "+jsonObj.getString("access_token");
				
			List<ResponseDTO> dataPoint = this.queryTS(params,autorization);
			Jedis redis = null;
			 redis = redisPool.getResource();
			 StringTokenizer tokens = new StringTokenizer("");
			for (ResponseDTO dto : dataPoint) {
				if(!dto.getDatapoints().isEmpty())
				for (DatapointDTO dtPoint : dto.getDatapoints()) {
//					 log.info("VALUE HARD FAIL "+dtPoint.getValue());
					if(dtPoint.getValue() == 1.0){
						try
					    {
					        redis.set(dto.getTag(), "highWarn");
					        redis.set(dto.getTag().concat("_TimestampH"),dtPoint.getTimeStamp()+"");
					        String gateway = null;
					        String sensorId = null;
					        tokens = new StringTokenizer(dto.getTag(), "_");
					        int nDatos = tokens.countTokens();
				            String[] datos = new String[nDatos];
				            int i=0;
				            while(tokens.hasMoreTokens()){
				                String str=tokens.nextToken();
				                datos[i]=str;
				               if(i==0){
				               gateway = datos[i];
				               }else if (i==1){
				            	   sensorId = datos[i];
				               }
				               i++;
				            }
				            if(redis.get(dto.getTag().concat("_emailHF"))==null){
				            	redis.set(dto.getTag().concat("_emailHF"),"0");
				            }
				            if(redis.get(dto.getTag().concat("_emailHF"))!=null && redis.get(dto.getTag().concat("_emailHF")).equals("0")){
				            this.sendEmailHF(gateway,sensorId);
				            redis.set(dto.getTag().concat("_emailHF"),"1");
				            }
					    }
					    catch (JedisConnectionException e)
					    {
					       e.printStackTrace();
					    }
					}else {
						try
					    {
					        redis.set(dto.getTag(), "noError");
					        redis.set(dto.getTag().concat("_TimestampH"),dtPoint.getTimeStamp()+"");
					    }
					    catch (JedisConnectionException e)
					    {
					       e.printStackTrace();
					    }
					}
				}else{
					log.info("NO DATA POINTS IN THE INTERVAL FOR THE TAG  "+dto.getTag());
				}
			}
		    redis.close();
		    redis = null;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
	}
	/**
	 * @param gateway
	 * @param sensorId -
	 * @param string 
	 * @throws IOException 
	 */
	private String sendEmailHF(String gateway, String sensorId) throws IOException {

		Properties prop = new Properties();
		String propFileName = "config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		final String username = prop.getProperty("mail.usr");
		final String password = prop.getProperty("mail.pwd");
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");

			Session session = Session.getInstance(props,
			  new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			  });

			try {

				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(username));
				message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(prop.getProperty("mail.adressListHF")));
				message.setSubject("FFU HARD FAILURE NOTIFICATION – Gateway "+gateway+", Sensor "+sensorId);
				message.setText("Edge Analytics have detected a possible Hard Failure of the Fan Filter Unit associated with "
						+ "Gateway "+gateway+", Sensor "+sensorId+".");
				message.setHeader("X-Priority", "1");
				Transport.send(message);

				System.out.println("Done");

			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}        return String.format("{\"status\":\"email Sent\", \"date\": \" " + new Date() + "\"}");


		
	}
	/**
	 *  -
	 * @param mapSF 
	 */
	private void softFailMonitor(TreeMap<String, String> mapSF) {
		try {
			log.info("Lets check TS");
			Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			QueryParamsDTO params = new QueryParamsDTO();
			params.setStart(new Timestamp(Calendar.getInstance().getTimeInMillis() - new Long(prop.getProperty("period.time.query"))));
			params.setEnd(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			params.setIntervalValue(0);
			List<String> tags = new ArrayList<>();
			tags.addAll(mapSF.values());
			params.setTagsArray(tags);
			
			JedisPool redisPool = new JedisPool(new JedisPoolConfig(), prop.getProperty("redis.host"),new Integer(prop.getProperty("redis.port")), Protocol.DEFAULT_TIMEOUT, prop.getProperty("redis.pwd"));
			String url = prop.getProperty("uaa.URI.token");
	     	   HttpClient client = HttpClientBuilder.create().build();
	     	   HttpPost post = new HttpPost(url);
	     	   post.setHeader("Authorization",prop.getProperty("uaa.basic.auth"));
	     	   post.setHeader("Content-Type","application/x-www-form-urlencoded");
	     	   post.setHeader("Accept","application/json");
	     	   
	     	   StringEntity postingString;
	     	   String body="grant_type=client_credentials";
	     	   	postingString = new StringEntity(body);
				   post.setEntity(postingString);
	     		   HttpResponse response = null;
				try {
					response = client.execute(post);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	     		   
	     		   int responseCode;
	     		   responseCode = response.getStatusLine().getStatusCode();
	     		   String result =null;
	     		   
	         		   try {
						result = EntityUtils.toString(response.getEntity());
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
	         		   
	         			   JSONObject jsonObj = new JSONObject(result);
	         			 String autorization ="Bearer "+jsonObj.getString("access_token");
			
			
			
			List<ResponseDTO> dataPoint = this.queryTS(params,autorization);
			Jedis redis = null;
			redis = redisPool.getResource();
			StringTokenizer tokens = new StringTokenizer("");
			for (ResponseDTO dto : dataPoint) {
				if(!dto.getDatapoints().isEmpty())
				for (DatapointDTO dtPoint : dto.getDatapoints()) {
					 if(dtPoint.getValue() == 1.0){
						try
					    {
							redis.set(dto.getTag(), "lowWarn");
					        redis.set(dto.getTag().concat("_TimestampS"),dtPoint.getTimeStamp()+"");
					        String gateway = null;
					        String sensorId = null;
					        tokens = new StringTokenizer(dto.getTag(), "_");
					        int nDatos = tokens.countTokens();
				            String[] datos = new String[nDatos];
				            int i=0;
				            while(tokens.hasMoreTokens()){
				                String str=tokens.nextToken();
				                datos[i]=str;
				               if(i==0){
				               gateway = datos[i];
				               }else if (i==1){
				            	   sensorId = datos[i];
				               }
				               i++;
				            }
				            if(redis.get(dto.getTag().concat("_emailSF"))==null){
				            	redis.set(dto.getTag().concat("_emailSF"),"0");
				            }
				            if(redis.get(dto.getTag().concat("_emailSF"))!=null && redis.get(dto.getTag().concat("_emailSF")).equals("0")){
				            	this.sendEmailSF(gateway,sensorId);
				            redis.set(dto.getTag().concat("_emailSF"),"1");
				            }
					    }
					    catch (JedisConnectionException e)
					    {
					       e.printStackTrace();
					    }
					}else {
						try
					    {
					        redis.set(dto.getTag(), "noError");
					        redis.set(dto.getTag().concat("_TimestampS"),dtPoint.getTimeStamp()+"");
					    }
					    catch (JedisConnectionException e)
					    {
					       e.printStackTrace();
					    }
					}
				}else{
					log.info("NO DATA POINTS IN THE INTERVAL FOR THE TAG  "+dto.getTag());
				}
			}
		    redis.close();
		    redis = null;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
	}
	/**
	 * @param params
	 * @param autorization 
	 * @return -
	 */
	private List<ResponseDTO> queryTS(QueryParamsDTO params, String authorization) {
		try {
			
        	TenantContext tenant = TenantContextFactory.createQueryTenantContextFromProvidedProperties
        			("https://time-series-store-predix.run.aws-usw02-pr.ice.predix.io/v1/datapoints",
        					authorization,this.PZID,this.tsInjectionWSConfig.getZoneId());
        	QueryBuilder builder = null;
        	builder = QueryBuilder.createQuery()
        	        .withStartAbs(params.getStart().getTime())
        	        .withEndAbs(params.getEnd().getTime())
        	        .addTags(QueryTag.Builder.createQueryTag().withTagNames(params.getTagsArray()).withLimit(100000).build());
	    	
        	QueryResponse response = ClientFactory.queryClientForTenant(tenant).queryAll(builder.build());
        	ResponseDTO dto = new ResponseDTO();
        	DatapointDTO dtpDto = new DatapointDTO();
        	List<DatapointDTO> dtoDtpList = new ArrayList<>();
        	List<ResponseDTO> dtoList = new ArrayList<>();
        	
        	for (TagResponse tagResp : response.getTags()) {
        		dto =  new ResponseDTO();
        		dto.setTag(tagResp.getName());
        		dtoDtpList = new ArrayList<>();
        		for (Result reslt : tagResp.getResults()) {
					for (DataPoint dtp : reslt.getDataPoints()) {
						dtpDto = new DatapointDTO();
						dtpDto.setTimeStamp(new Timestamp(dtp.getTimestamp()));
						dtpDto.setQuality(dtp.getQuality().getName());
						dtpDto.setValue(new Double(dtp.getValue().toString()));
						dtoDtpList.add(dtpDto);
					}
					dto.setDatapoints(dtoDtpList);
				}
        		dtoList.add(dto);
        	}
        	tenant = null;
        	builder = null;
        	response = null;
        	return dtoList;
        } catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			System.gc();
			Runtime.getRuntime().gc();
		}
	}
	/**
	 * @return -
	 */
	public SensorDTO gettingSensorSatus() {
		Jedis redis = null;
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
			SensorDTO sensorDto = new SensorDTO();
			redis = redisPool.getResource();
			sensorDto = new SensorDTO();
			
			
			
			TreeMap<String, String> mapHFSF = new TreeMap<>();
			for (String tags : this.getTags().getResults()) {
				if(tags.contains(prop.getProperty("filterTag.HFD"))){
					mapHFSF.put(tags, tags);
				}else if (tags.contains(prop.getProperty("filterTag.HBF"))){
					mapHFSF.put(tags, tags);
				}else if (tags.contains(prop.getProperty("filterTag.HB"))){
					mapHFSF.put(tags, tags);
				}else if (tags.contains(prop.getProperty("filterTag.SFD"))){
					mapHFSF.put(tags, tags);
				}
			}
			TreeMap<String, String> filterMap = new TreeMap<>();
			for (String tagName : mapHFSF.values()) {
					    try
					    {
					    	TagStatusDTO tagSTS = new TagStatusDTO();
					    	tagSTS.setName(tagName.substring(8,14));
					    	tagSTS.setTool(tagName.substring(0,7));
					    	if(prop.getProperty(tagSTS.getTool())!=null)
					    	tagSTS.setArea(prop.getProperty(tagSTS.getTool()));
					    	else
					    		tagSTS.setArea(prop.getProperty("default.area"));
					    	String gtID = tagSTS.getTool().concat("_").concat(tagSTS.getName());
					    	if(redis.get(gtID.concat(prop.getProperty("tag.HFD")))!=null && !redis.get(gtID.concat(prop.getProperty("tag.HFD"))).equals("noError")){
					        	tagSTS.setErrorStatus(redis.get(gtID.concat(prop.getProperty("tag.HFD"))));
					        }else if(redis.get(gtID.concat(prop.getProperty("tag.HBF")))!=null && !redis.get(gtID.concat(prop.getProperty("tag.HBF"))).equals("noError")){
					        	tagSTS.setErrorStatus(redis.get(gtID.concat(prop.getProperty("tag.HBF"))));
					        }else if(redis.get(gtID.concat(prop.getProperty("tag.SFD")))!=null &&  !redis.get(gtID.concat(prop.getProperty("tag.SFD"))).equals("noError")){
					        	tagSTS.setErrorStatus(redis.get(gtID.concat(prop.getProperty("tag.SFD"))));
					        }else{
					        	tagSTS.setErrorStatus("noError");
					        }
					    	if(!filterMap.containsKey(gtID)){
					        sensorDto.getFanUnits().add(tagSTS);
					    	filterMap.put(gtID, gtID);
					    	}
					    }
					    catch (JedisConnectionException e)
					    {
					       e.printStackTrace();
					    }
					}
   				
				redisPool.close();
				return sensorDto;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			 redis.close();
		}
		return null;
	}
	/**
	 *  -
	 */
	private TagsDTO getTags() {
		
		 try {
			 Properties prop = new Properties();
				String propFileName = "config.properties";
				InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
				if (inputStream != null) {
					prop.load(inputStream);
				} else {
					throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
				}
			 
			   String url = prop.getProperty("uaa.URI.token");
	     	   HttpClient client = HttpClientBuilder.create().build();
	     	   HttpPost post = new HttpPost(url);
	     	   post.setHeader("Authorization",prop.getProperty("uaa.basic.auth"));
	     	   post.setHeader("Content-Type","application/x-www-form-urlencoded");
	     	   post.setHeader("Accept","application/json");
	     	   
	     	   StringEntity postingString;
	     	   String body="grant_type=client_credentials";
	     	   	postingString = new StringEntity(body);
				   post.setEntity(postingString);
	     		   HttpResponse response = null;
				try {
					response = client.execute(post);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	     		   
	     		   int responseCode;
	     		   responseCode = response.getStatusLine().getStatusCode();
	     		   String result =null;
	     		   if (responseCode==200){
	         		   try {
						result = EntityUtils.toString(response.getEntity());
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
	         		   
	         			   JSONObject jsonObj = new JSONObject(result);
	         			 String token ="Bearer "+jsonObj.getString("access_token");

			 url = prop.getProperty("time.series.V1tags");
			client = HttpClientBuilder.create().build();
			HttpGet  get = new HttpGet (url);
			get.setHeader("Authorization", token);
			get.setHeader("Content-Type", "application/json");
			get.setHeader("Predix-Zone-Id", prop.getProperty("time.series.zoneId"));
			get.setHeader("Accept","application/json");
			try {
				response = client.execute(get);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				result = EntityUtils.toString(response.getEntity());
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
				ObjectMapper mapper = new ObjectMapper();

				try {
					
					TagsDTO tags = mapper.readValue(result, TagsDTO.class);
					return tags;
				
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		 }catch (Exception e) {
			 e.printStackTrace();
		}
		return null;
		
	}
	/**
	 * @param param
	 * @return -
	 */
	public SFailuresDTO softFailures(FanParamsDTO param) {
		try {
			log.info("GATEWAY_FAN= "+param.getGateway().concat("_").concat(param.getFan().concat("_")));
			String	authorization = null;
			String gwFan = param.getGateway().concat("_").concat(param.getFan().concat("_"));
			try {
			List<Header> lHead = restClient.getSecureTokenForClientId();
        	for (Header header : lHead) {
        		authorization = header.getValue();
			}
        	} catch (Exception e) {
				// TODO: handle exception
			}
        	TenantContext tenant = TenantContextFactory.createQueryTenantContextFromProvidedProperties
        			("https://time-series-store-predix.run.aws-usw02-pr.ice.predix.io/v1/datapoints",
        					authorization,this.PZID,this.tsInjectionWSConfig.getZoneId());
        QueryBuilder builder = null;
        
        List<String> tsTagsList = new ArrayList<>();
        String key = null;
        for (String tsTags : TAGS) {
			key = gwFan.concat(tsTags);
			tsTagsList.add(key);
		}
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
		FailuresStsDTO failuresDto = new FailuresStsDTO();
		SFailuresDTO dto = new  SFailuresDTO();
    	failuresDto.setResult("Timestamp");
    	log.info("GATEWAY_FAN_REDIS "+gwFan.concat("Soft_Fail_Detected_TimestampS"));
    	failuresDto.setValue(redis.get(gwFan.concat("Soft_Fail_Detected_TimestampS")).concat(prop.getProperty("sdf.timeStampHMS")));
		failuresDto.setUnits("");
		dto.getSoftFailures().add(failuresDto);
		
		Timestamp timestamp = null;
		try{
		    SimpleDateFormat dateFormat = new SimpleDateFormat(prop.getProperty("sdf.timeStamp"));
		    Date parsedDate = dateFormat.parse(redis.get(gwFan.concat("Soft_Fail_Detected_TimestampS")));
		    timestamp = new java.sql.Timestamp(parsedDate.getTime());
		}catch(Exception e){//this generic but you can control another types of exception
		
		}
			builder = QueryBuilder.createQuery()
        	        .withStartAbs(timestamp.getTime()-60000)
        	        .withEndAbs(timestamp.getTime()+60000)
        	        .addTags(QueryTag.Builder.createQueryTag().withTagNames(tsTagsList).withLimit(100000).build());
	    	
        	QueryResponse response = ClientFactory.queryClientForTenant(tenant).queryAll(builder.build());
        	String sHealth = null;
        	TreeMap<String, String> mapTags = new TreeMap<>();
        	for (TagResponse tagResp : response.getTags()) {
        		log.info("RESPONSE NAME: "+tagResp.getName());
        		failuresDto = new FailuresStsDTO();
        		failuresDto.setResult(tagResp.getName().replace("Soft_Fail_Detected","Status")
        				.replace("Differential_Energy_Reference","Diff Energy Reference").replace("Differential_Energy","Diff Energy Average")
        				.replace("DE_Alarm_Level","Diff Energy Alarm Level").replace("X_Acceleration_Avg","X Acceleration Average")
        				.replace("Y_Acceleration_Avg","Y Acceleration Average").replace("Z_Acceleration_Avg","Z Acceleration Average").replace(gwFan, ""));
        		for (Result reslt : tagResp.getResults()) {
					for (DataPoint dtp : reslt.getDataPoints()) {
						sHealth = dtp.getQuality().toString();
						if(failuresDto.getResult().contains("Status") && dtp.getValue().toString().equals("0") )
							failuresDto.setValue("Ok");
						else if (failuresDto.getResult().contains("Status") && dtp.getValue().toString().equals("1")) {
							failuresDto.setValue("Fault");
							}
							else
								failuresDto.setValue(dtp.getValue().toString());
						
						if(failuresDto.getResult().contains("Status")
								|| failuresDto.getResult().contains("DE Alarm")
									|| failuresDto.getResult().contains("Sensor Health") )
						failuresDto.setUnits("");
						else
							failuresDto.setUnits("g");
						if(!mapTags.containsKey(tagResp.getName().replace("Soft_Fail_Detected","Status")
		        				.replace("Differential_Energy_Reference","Diff Energy Reference").replace("Differential_Energy","Diff Energy Average")
		        				.replace("DE_Alarm_Level","Diff Energy Alarm Level").replace("X_Acceleration_Avg","X Acceleration Average")
		        				.replace("Y_Acceleration_Avg","Y Acceleration Average").replace("Z_Acceleration_Avg","Z Acceleration Average").replace(gwFan, ""))){
								dto.getSoftFailures().add(failuresDto);
								mapTags.put(tagResp.getName().replace("HardFailDetected", "Status")
		        				.replace("AvgResult", "Change in Average").replace("MeanResult", "Change in Mean")
		        				.replace("StdDevResult","Change in Std Deviation").replace("HeartBeat","Sensor Fault").replace(gwFan, ""), tagResp.getName().replace("HardFailDetected", "Status")
		        				.replace("AvgResult", "Change in Average").replace("MeanResult", "Change in Mean")
		        				.replace("StdDevResult","Change in Std Deviation").replace("HeartBeat","Sensor Fault").replace(gwFan, ""));
								}
					}
				}
        	}
        	
        	failuresDto = new FailuresStsDTO();
        	failuresDto.setResult("Sensor Health");
        	failuresDto.setValue(sHealth);
    		failuresDto.setUnits("");
    		dto.getSoftFailures().add(failuresDto);
    		
        	tenant = null;
        	builder = null;
        	response = null;
        	return dto;
        } catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			System.gc();
			Runtime.getRuntime().gc();
		}
	}
	/**
	 * @param param
	 * @return -
	 */
	public HFailuresDTO hardFailures(FanParamsDTO param) {
		try {
			String	authorization = null;
			String gwFan = param.getGateway().concat("_").concat(param.getFan().concat("_"));
			try {
			List<Header> lHead = restClient.getSecureTokenForClientId();
        	for (Header header : lHead) {
        		authorization = header.getValue();
			}
        	} catch (Exception e) {
				// TODO: handle exception
			}
        	TenantContext tenant = TenantContextFactory.createQueryTenantContextFromProvidedProperties
        			("https://time-series-store-predix.run.aws-usw02-pr.ice.predix.io/v1/datapoints",
        					authorization,this.PZID,this.tsInjectionWSConfig.getZoneId());
        QueryBuilder builder = null;
        
        List<String> tsTagsList = new ArrayList<>();
        String key = null;
        for (String tsTags : HARDTAGS) {
			key = gwFan.concat(tsTags);
			tsTagsList.add(key);
		}
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
		FailuresStsDTO failuresDto = new FailuresStsDTO();
		HFailuresDTO dto = new  HFailuresDTO();
    	failuresDto.setResult("Timestamp");
    	failuresDto.setValue(redis.get(gwFan.concat("HardFailDetected_TimestampH")).concat(prop.getProperty("sdf.timeStampHMS")));
		failuresDto.setUnits("");
		dto.getHardFailures().add(failuresDto);
		
		Timestamp timestamp = null;
		try{
		    SimpleDateFormat dateFormat = new SimpleDateFormat(prop.getProperty("sdf.timeStamp"));
		    Date parsedDate = dateFormat.parse(redis.get(gwFan.concat("HardFailDetected_TimestampH")));
		    timestamp = new java.sql.Timestamp(parsedDate.getTime());
		}catch(Exception e){//this generic but you can control another types of exception
		
		}
			builder = QueryBuilder.createQuery()
        	        .withStartAbs(timestamp.getTime()-60000)
        	        .withEndAbs(timestamp.getTime()+60000)
        	        .addTags(QueryTag.Builder.createQueryTag().withTagNames(tsTagsList).withLimit(100000).build());
			QueryResponse response = ClientFactory.queryClientForTenant(tenant).queryAll(builder.build());
        	TreeMap<String, String> mapTags = new TreeMap<>();
        	for (TagResponse tagResp : response.getTags()) {
        		failuresDto = new FailuresStsDTO();
        		log.info("TS TAG NAME..... "+tagResp.getName());
        		failuresDto.setResult(tagResp.getName().replace("HardFailDetected", "Status")
        				.replace("AvgResult", "Change in Average").replace("MeanResult", "Change in Mean")
        				.replace("StdDevResult","Change in Std Deviation").replace("HeartBeat_Fault","Sensor Fault").replace(gwFan, ""));
        		for (Result reslt : tagResp.getResults()) {
					for (DataPoint dtp : reslt.getDataPoints()) {
						log.info("TAG NAME>>>>>>"+failuresDto.getResult()+  "STATUS VALUE>:>>>>>>>>>>>> "+dtp.getValue().toString());
						if(failuresDto.getResult().contains("Status") && dtp.getValue().toString().equals("0") )
							failuresDto.setValue("Ok");
						else if (failuresDto.getResult().contains("Status") && dtp.getValue().toString().equals("1")) {
							failuresDto.setValue("Fault");
						}else if(failuresDto.getResult().contains("Sensor Fault") && dtp.getValue().toString().equals("0") )
							failuresDto.setValue("Ok");
						else if (failuresDto.getResult().contains("Sensor Fault") && dtp.getValue().toString().equals("1")) {
							failuresDto.setValue("Fault");
						}else
							failuresDto.setValue(dtp.getValue().toString());
						
						if(failuresDto.getResult().contains("Change in Mean")
								|| failuresDto.getResult().contains("Change in Average")
									|| failuresDto.getResult().contains("Change in Std Deviation") )
						failuresDto.setUnits("g");
						else
							failuresDto.setUnits("");
						if(!mapTags.containsKey(tagResp.getName().replace("HardFailDetected", "Status")
        				.replace("AvgResult", "Change in Average").replace("MeanResult", "Change in Mean")
        				.replace("StdDevResult","Change in Std Deviation").replace("HeartBeat","Sensor Fault").replace(gwFan, ""))){
						dto.getHardFailures().add(failuresDto);
						mapTags.put(tagResp.getName().replace("HardFailDetected", "Status")
        				.replace("AvgResult", "Change in Average").replace("MeanResult", "Change in Mean")
        				.replace("StdDevResult","Change in Std Deviation").replace("HeartBeat","Sensor Fault").replace(gwFan, ""), tagResp.getName().replace("HardFailDetected", "Status")
        				.replace("AvgResult", "Change in Average").replace("MeanResult", "Change in Mean")
        				.replace("StdDevResult","Change in Std Deviation").replace("HeartBeat","Sensor Fault").replace(gwFan, ""));
						}
					}
				}
        	}
        	tenant = null;
        	builder = null;
        	response = null;
        	return dto;
        } catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			System.gc();
			Runtime.getRuntime().gc();
		}
	}
	/**
	 * @param param
	 * @return -
	 * @throws IOException 
	 */
	public String sendEmail(FanParamsDTO param) throws IOException {
		Properties prop = new Properties();
		String propFileName = "config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		final String username = prop.getProperty("mail.usr");
		final String password = prop.getProperty("mail.pwd");
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");

			Session session = Session.getInstance(props,
			  new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			  });

			try {

				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(username));
				message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(prop.getProperty("mail.dummyAdress")));
				message.setSubject("FFU HARD FAILURE NOTIFICATION – Gateway "+param.getGateway()+", Sensor "+param.getFan());
				message.setText("Edge Analytics have detected a possible Hard Failure of the Fan Filter Unit associated with "
						+ "Gateway "+param.getGateway()+", Sensor "+param.getFan()+".");
				message.setHeader("X-Priority", "1");
				Transport.send(message);

				System.out.println("Done");

			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}        return String.format("{\"status\":\"email Sent\", \"date\": \" " + new Date() + "\"}");

	}
	/**
	 * @param param
	 * @return -
	 * @throws IOException 
	 */
	public String sendEmailSF(FanParamsDTO param) throws IOException {

		Properties prop = new Properties();
		String propFileName = "config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		final String username = prop.getProperty("mail.usr");
		final String password = prop.getProperty("mail.pwd");
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");

			Session session = Session.getInstance(props,
			  new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			  });

			try {

				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(username));
				message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(prop.getProperty("mail.dummyAdress")));
				message.setSubject("FFU Soft Failure Notification  – Gateway "+ param.getGateway()+", Sensor "+param.getFan());
				message.setText("Edge Analytics have detected possible conditions leading to failure of the FFU associated with Gateway "+ param.getGateway()+", Sensor "+param.getFan()+".");
				message.setHeader("X-Priority", "3");
				Transport.send(message);

				System.out.println("Done");

			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}        return String.format("{\"status\":\"email Sent\", \"date\": \" " + new Date() + "\"}");


	}
	
	
	/**
	 * @param string 
	 * @param param
	 * @return -
	 * @throws IOException 
	 */
	public String sendEmailSF(String gateway,String sensorId) throws IOException {
		Properties prop = new Properties();
		String propFileName = "config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		final String username = prop.getProperty("mail.usr");
		final String password = prop.getProperty("mail.pwd");
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");

			Session session = Session.getInstance(props,
			  new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			  });

			try {
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(username));
				message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(prop.getProperty("mail.adressListSF")));
				message.setSubject("FFU Soft Failure Notification  – Gateway "+gateway+", Sensor "+sensorId);
				message.setText("Edge Analytics have detected possible conditions leading to failure of the FFU associated with Gateway "+gateway+", Sensor "+sensorId+".");
				message.setHeader("X-Priority", "3");
				Transport.send(message);

				

			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}        return String.format("{\"status\":\"email Sent\", \"date\": \" " + new Date() + "\"}");


	}
	
	
		

	
		
		

    
	
}
