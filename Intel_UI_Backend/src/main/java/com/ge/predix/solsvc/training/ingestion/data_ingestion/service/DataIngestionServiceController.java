package com.ge.predix.solsvc.training.ingestion.data_ingestion.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ge.predix.solsvc.training.ingestion.data_ingestion.api.DataIngestionServiceAPI;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.AdresseeDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.FanParamsDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.HFailuresDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.IngestDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.ParamsDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.SFailuresDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.SensorDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.SimpleResponseDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.handler.DataIngestionHandler;
import com.ge.predix.solsvc.training.query.data_query.handler.DataQueryHandler;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * 
 * @author predix -
 */
@RestController
@SuppressWarnings("nls")
public class DataIngestionServiceController extends TimerTask   implements DataIngestionServiceAPI
{
    private static Logger        log       = LoggerFactory.getLogger(DataIngestionServiceController.class);

    @Autowired
    private DataIngestionHandler dataIngestionHandler;
    @Autowired
    private DataQueryHandler dataQueryHandler;

    @RequestMapping(value="/ping",method=RequestMethod.GET)
	public String ping() {
		return "SUCCESS"; //$NON-NLS-1$
	}
    
    @RequestMapping(value="/resetSensor",method=RequestMethod.POST,produces = "application/json")
   	public @ResponseBody SimpleResponseDTO resetSensor(@RequestHeader(value = "Authorization", required = true) String authorization,  @RequestBody FanParamsDTO sensor) {
    	try
        {
    	return this.dataIngestionHandler.resetSensor(sensor,authorization);
        }catch (Throwable e){
            log.error("Failure in /resetSensor POST ", e);
            return null;
        }
    }
    
    @RequestMapping(value="/resetSensorV2",method=RequestMethod.POST,produces = "application/json")
   	public @ResponseBody SimpleResponseDTO resetSensorV2(@RequestHeader(value = "Authorization", required = true) String authorization,  @RequestBody FanParamsDTO sensor) {
    	try
        {
    		SimpleResponseDTO resp = new SimpleResponseDTO();
    		if(this.checkTokenV2(authorization)!=200){
    			resp.setResponse("Invalid Token");
    			return resp;
    		}
    	return this.dataIngestionHandler.resetSensor(sensor,authorization);
        }catch (Throwable e){
            log.error("Failure in /resetSensor POST ", e);
            return null;
        }
    }
   
    
    /**
	 * @param authorization
	 * @return -
	 */
	private int checkTokenV2(String authorization) {
		int responseCode=0;
		try
        {
			Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
		String url = prop.getProperty("uaa.URI.checkToken");
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.setHeader("Authorization", prop.getProperty("uaa.Authorization.checkToken"));
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		StringEntity postingString;
  	    String body="token="+authorization;
  		   postingString = new StringEntity(body);
  		   post.setEntity(postingString);
  		   HttpResponse response = client.execute(post);
  		 return response.getStatusLine().getStatusCode();
			
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return responseCode;
		} catch (IOException e) {
			e.printStackTrace();
			return responseCode;
		}
	}

	/**
	 * @param authorization
	 * @return -
	 */
	private int checkToken(String authorization) {
		int responseCode=0;
//		try
//        {
//			Properties prop = new Properties();
//			String propFileName = "config.properties";
//			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
//			if (inputStream != null) {
//				prop.load(inputStream);
//			} else {
//				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
//			}
//		String url = prop.getProperty("uaa.URI.checkToken");
//		HttpClient client = HttpClientBuilder.create().build();
//		HttpPost post = new HttpPost(url);
//		post.setHeader("Authorization", prop.getProperty("uaa.Authorization.checkToken"));
//		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
//		StringEntity postingString;
//  	    String body="token="+authorization;
//  		   postingString = new StringEntity(body);
//  		   post.setEntity(postingString);
//  		   HttpResponse response = client.execute(post);
//  		 return response.getStatusLine().getStatusCode();
			return 200;
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//			return responseCode;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return responseCode;
//		}

	}

	@RequestMapping(value="/ingestTS",method=RequestMethod.POST,produces = "application/json")
   	public @ResponseBody String ingestTS(@RequestHeader (value = "Authorization", required = true) String authorization, @RequestBody List<ParamsDTO> params) {
    	try
        {
    		if(this.checkToken(authorization)!=200){
    			return "Invalid Token";
    		}
     	 return this.dataIngestionHandler.ingestTS(params);
        }catch (Throwable e){
            log.error("Failure in /ingestFiles POST ", e);
            return "Failure in /ingestFiles POST "+ e.getMessage();
        }
    }
    
    @RequestMapping(value="/startTSMonitor",method=RequestMethod.POST,produces = "application/json")
   	public String startMonitorTS(@RequestHeader (value = "Authorization", required = true) String authorization) {
    	try
        {
    		
    		if(this.checkToken(authorization)!=200){
    			return "Invalid Token";
    		}
    		Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			try {
    			Timer t = new Timer();
    			DataIngestionServiceController task = new DataIngestionServiceController();
    			t.scheduleAtFixedRate(task, 0, new Long (prop.getProperty("periodic.basis")));
    		} catch (Exception e) {
    			e.printStackTrace();
    			return "Error";
    		}
    		return "SUCCESS";
    	}catch (Throwable e){
            log.error("Failure in /startTSMonitor POST ", e);
            return "Error";
        }
    }
    @RequestMapping(value="/startMonitor",method=RequestMethod.POST,produces = "application/json")
   	public void startMonitor() {
    	try {
			this.dataQueryHandler.startTSMonitor();
		} catch (Exception e) {
			log.error("Failure in /startTSMonitor POST ", e);
		}
    }
    
    
    @RequestMapping(value="/sensorStatus",method=RequestMethod.POST,produces = "application/json")
   	public SensorDTO gettingSensorSatus(@RequestHeader (value = "Authorization", required = true) String authorization) {
    	try
        {
    		if(this.checkToken(authorization)!=200){
    			log.error("Invalid Token");
    			return null;
    		}
    		
		return this.dataQueryHandler.gettingSensorSatus();
        }catch (Throwable e){
            log.error("Failure in /sensorStatus POST ", e);
            return null;
        }
    }


    @RequestMapping(value="/softFailures",method=RequestMethod.POST,produces = "application/json")
   	public @ResponseBody SFailuresDTO softFailures(@RequestHeader (value = "Authorization", required = true) String authorization,@RequestBody FanParamsDTO param) {
    	try
        {
    		if(this.checkToken(authorization)!=200){
    			log.error("Invalid Token");
    			return null;
    		}
     	 return this.dataQueryHandler.softFailures(param);
        }catch (Throwable e){
            log.error("Failure in /softFailures POST ", e);
            return null;
        }
    }
    
    @RequestMapping(value="/hardFailures",method=RequestMethod.POST,produces = "application/json")
   	public @ResponseBody HFailuresDTO hardFailures(@RequestHeader (value = "Authorization", required = true) String authorization,@RequestBody FanParamsDTO param) {
    	try
        {
    		if(this.checkToken(authorization)!=200){
    			log.error("Invalid Token");
    			return null;
    		}
     	 return this.dataQueryHandler.hardFailures(param);
        }catch (Throwable e){
            log.error("Failure in /hardFailures POST ", e);
            return null;
        }
    }
    
    
    
    @RequestMapping(value="/ingestFiles",method=RequestMethod.POST,produces = "application/json")
	public String ingestFiles(@RequestHeader (value = "Authorization", required = true) String authorization) {
    	try {
    		try {
    			if(this.checkToken(authorization)!=200){
        			log.error("Invalid Token");
        			return "Invalid Token";
        		}
    			log.info("INGESTION PROCESS STARTED");
    			String path = "/predix/emailFiles"; 
    	        String files;
    	        File folder = new File(path);
    	        File[] listOfFiles = folder.listFiles(); 
    	        List<String> fileNames =  new ArrayList<String>();
    	        for (int i = 0; i < listOfFiles.length; i++)         {

    	            if (listOfFiles[i].isFile())             {
    	                files = listOfFiles[i].getName();
    	                if(files.contains("softFailures"))
    	                	fileNames.add(files);
    	            }
    	        }
    			
    		String cadena;
    		for (String fileNameVar : fileNames) {
			ZipFile zf = new ZipFile("/predix/emailFiles/".concat(fileNameVar));
			for (Enumeration entries = zf.entries(); entries.hasMoreElements();) {
				IngestDTO ingestDto = new IngestDTO();
				List<IngestDTO> ingestList = new ArrayList<>();
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String zipEntryName = entry.getName();
				InputStream in = zf.getInputStream(entry);
				OutputStream out = new FileOutputStream("/predix/unzipedEmails/" + zipEntryName);
				
				byte[] buf1 = new byte[1024];
				int len;
				while ((len = in.read(buf1)) > 0) {
					out.write(buf1, 0, len);
				}
				in.close();
				out.close();
				
//			  System.out.println("FILE NAME>>>>> "+zipEntryName);
				FileReader f = new FileReader("/predix/unzipedEmails/"+zipEntryName);
		        BufferedReader b = new BufferedReader(f);
		        StringTokenizer tokens = new StringTokenizer("");
		        String tagId = null;
		        while((cadena = b.readLine())!=null) {
//		        	System.out.println("CADENA "+cadena);
		        	cadena = cadena.replace(",,", ",ND,").replace(".0,",",");
		            tokens = new StringTokenizer(cadena, ",");
		            int nDatos = tokens.countTokens();
		            String[] datos = new String[nDatos];
		            int i=0;
		            ingestDto = new IngestDTO();
		            
		            while(tokens.hasMoreTokens()){
		                String str=tokens.nextToken();
		                datos[i]=str;
		               if(i==0){
//		            	   System.out.println("TSTAMP "+datos[i].substring(0, 13));
		                ingestDto.setTimestamp(new Timestamp(new Long(datos[i].substring(0, 13))));
		                
		               }
		               else if(i==1){
		            	   
		            	   if(!datos[i].equals("ND")){
		            		   tagId = datos[1];
		            	   }
		            	   ingestDto.setTagId(datos[i]);
		            	   if(!ingestDto.getTagId().equals("ND")){
		            		   ingestList.add(ingestDto);
		            		   }
		            	   else{
		            		   ingestDto.setTagId(tagId);
		            		   ingestList.add(ingestDto);
		            	   }
		               }
		               else if(i==2){
		            	   ingestDto.setValue(new Double(datos[i]));
		               }
		               else if(i==3){
		               		ingestDto.setQuality(new Integer(datos[i]));
		               }
		                i++;
		            }
		        }
		        
		        
		        out = null;
		        in = null;
		        b.close();
		        cadena = null;
		        
		        this.dataIngestionHandler.ingestFilesIntoTS(ingestList);
		       
		        File fichero = new File("/predix/unzipedEmails/" + zipEntryName);
		        fichero.delete();
		        fichero = null;
		        f = null;
		        b = null;
		        
		        
				String ruta = "/predix/unzipedEmails/report";
				File archivo = new File(ruta);
				FileWriter escribir = new FileWriter(archivo,true);
				if(archivo.exists()) {
					    escribir.append("\r\n");
						escribir.write(zipEntryName);
				} else {
					escribir.append("\r\n");
					escribir.write(zipEntryName);
				}
				escribir.close();
		        
			}
			
			}
			
			
    	}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
    	return "SUCCESS"; //$NON-NLS-1$
	}



	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		try {
			log.info("Monitor is working");
			String url = "https://emailnotifications-app.run.aws-usw02-pr.ice.predix.io/startMonitor";
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/json");
			HttpResponse response = client.execute(post);
	  		 
		} catch (Exception e) {
			log.error("Failure in /startTSMonitor POST ", e);
		}
		
	}
	
	@RequestMapping(value="/sendEmailHF",method=RequestMethod.POST,produces = "application/json")
   	public @ResponseBody String senEmailHF(@RequestHeader (value = "Authorization", required = true) String authorization,@RequestBody FanParamsDTO param) {
    	try
        {
    		if(this.checkToken(authorization)!=200){
    			log.error("Invalid Token");
    			return "Invalid Token";
    		}
     	 return this.dataQueryHandler.sendEmail(param);
        }catch (Throwable e){
            log.error("Failure in /sendEmail POST ", e);
            return "Error";
        }
    }
	
	@RequestMapping(value="/sendEmailSF",method=RequestMethod.POST,produces = "application/json")
   	public @ResponseBody String senEmailSF(@RequestHeader (value = "Authorization", required = true) String authorization,@RequestBody FanParamsDTO param) {
    	try
        {
    		if(this.checkToken(authorization)!=200){
    			log.error("Invalid Token");
    			return "Invalid Token";
    		}
     	 return this.dataQueryHandler.sendEmailSF(param);
        }catch (Throwable e){
            log.error("Failure in /sendEmail POST ", e);
            return "Error";
        }
    }

	/* (non-Javadoc)r
	 * @see com.ge.predix.solsvc.training.ingestion.data_ingestion.api.DataIngestionServiceAPI#ingestTS(java.util.List)
	 */
	@Override
	public String ingestTS(List<ParamsDTO> params) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ge.predix.solsvc.training.ingestion.data_ingestion.api.DataIngestionServiceAPI#ingestFiles()
	 */
	@Override
	public String ingestFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ge.predix.solsvc.training.ingestion.data_ingestion.api.DataIngestionServiceAPI#startMonitorTS()
	 */
	@Override
	public void startMonitorTS() {
		// TODO Auto-generated method stub
		
	}
	
	
}
