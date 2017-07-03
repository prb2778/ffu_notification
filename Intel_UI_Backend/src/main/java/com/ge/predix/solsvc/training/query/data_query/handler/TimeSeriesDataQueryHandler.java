/*
 * Copyright (c) 2017 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */
 
package com.ge.predix.solsvc.training.query.data_query.handler;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;



import org.apache.http.Header;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ge.predix.solsvc.bootstrap.tsb.client.TimeseriesWSConfig;
import com.ge.predix.solsvc.restclient.impl.RestClient;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.DatapointDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.QueryParamsDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.ResponseDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.handler.BaseFactoryIT;
import com.ge.predix.timeseries.client.ClientFactory;
import com.ge.predix.timeseries.client.TenantContext;
import com.ge.predix.timeseries.client.TenantContextFactory;
import com.ge.predix.timeseries.model.builder.Aggregation;

import com.ge.predix.timeseries.model.builder.QueryBuilder;
import com.ge.predix.timeseries.model.builder.QueryTag;
import com.ge.predix.timeseries.model.builder.TimeUnit;
import com.ge.predix.timeseries.model.datapoints.DataPoint;
import com.ge.predix.timeseries.model.response.QueryResponse;
import com.ge.predix.timeseries.model.response.Result;
import com.ge.predix.timeseries.model.response.TagResponse;



/**
 * 
 * @author predix -
 */
@Component
public class TimeSeriesDataQueryHandler extends BaseFactoryIT {
	
	private static Logger log = Logger.getLogger(TimeSeriesDataQueryHandler.class);
    
	@Autowired
	private RestClient restClient;
	
	private static final String PZID = "Predix-Zone-Id";
	
	@Autowired
	private TimeseriesWSConfig tsInjectionWSConfig;

	/**
	 * @param params -
	 */
	public List<ResponseDTO> queryTSByInterval(QueryParamsDTO params) {
		try {
			
			String	authorization = null;
        	List<Header> lHead = restClient.getSecureTokenForClientId();
        	for (Header header : lHead) {
        		authorization = header.getValue();
			}
        	TenantContext tenant = TenantContextFactory.createQueryTenantContextFromProvidedProperties
        			("https://time-series-store-predix.run.aws-usw02-pr.ice.predix.io/v1/datapoints",
        					authorization,this.PZID,this.tsInjectionWSConfig.getZoneId());
        QueryBuilder builder = null;
        if(params.getIntervalValue()==0){
        	builder = QueryBuilder.createQuery()
        	        .withStartAbs(params.getStart().getTime())
        	        .withEndAbs(params.getEnd().getTime())
        	        .addTags(QueryTag.Builder.createQueryTag().withTagNames(params.getTagsArray()).withLimit(100000)
        	        		.addAggregation(Aggregation.Builder.averageWithInterval(1, TimeUnit.MINUTES))
        	                        .build());
	    }else{
        builder = QueryBuilder.createQuery()
            	        .withStartAbs(params.getStart().getTime())
            	        .withEndAbs(params.getEnd().getTime())
            	        .addTags(QueryTag.Builder.createQueryTag().withTagNames(params.getTagsArray()).withLimit(100000)
            	        		.addAggregation(Aggregation.Builder.interpolateWithInterval(params.getIntervalValue(), TimeUnit.MINUTES))
            	                        .build());
		}
        	QueryResponse response = ClientFactory.queryClientForTenant(tenant).queryAll(builder.build());
        	String tms = null;
        	
        	ResponseDTO dto = new ResponseDTO();
        	DatapointDTO dtpDto = new DatapointDTO();
        	List<DatapointDTO> dtoDtpList = new ArrayList<>();
        	List<DatapointDTO> dtoDtpListLess = new ArrayList<>();
        	List<ResponseDTO> dtoList = new ArrayList<>();
        	TreeMap<Timestamp,DatapointDTO> mapCTF1 = new TreeMap<>();
    		TreeMap<Timestamp,DatapointDTO> mapCTF2 = new TreeMap<>();
    		TreeMap<Timestamp,DatapointDTO> mapCTD2 = new TreeMap<>();
    		TreeMap<Timestamp,DatapointDTO> mapCTD3 = new TreeMap<>();
    		TreeMap<Timestamp,DatapointDTO> mapCTDA1 = new TreeMap<>();
            for (TagResponse tagResp : response.getTags()) {
        		dto =  new ResponseDTO();
        		dto.setTag(tagResp.getName());
        		dtoDtpList = new ArrayList<>();
        		dtoDtpListLess = new ArrayList<>();
        		TreeMap<String,DatapointDTO> map = new TreeMap<>();
        		TreeMap<String,DatapointDTO> mapLess = new TreeMap<>();
        		
        		for (Result reslt : tagResp.getResults()) {
					for (DataPoint dtp : reslt.getDataPoints()) {
						
						dtpDto = new DatapointDTO();
						dtpDto.setTimeStamp(new Timestamp(dtp.getTimestamp()));
						dtpDto.setQuality(dtp.getQuality().getName());
						dtpDto.setValue(new Double(dtp.getValue().toString()));
						if(tagResp.getName().contains("CTIF1")){
							if(!mapCTF1.containsKey(dtpDto.getTimeStamp())){
								mapCTF1.put(dtpDto.getTimeStamp(), dtpDto);
							}
						}else if(tagResp.getName().contains("CTIF2")){
							if(!mapCTF2.containsKey(dtpDto.getTimeStamp())){
								mapCTF2.put(dtpDto.getTimeStamp(), dtpDto);
							}
						}else if(tagResp.getName().contains("CTDA2")){
							if(!mapCTD2.containsKey(dtpDto.getTimeStamp())){
								mapCTD2.put(dtpDto.getTimeStamp(), dtpDto);
							}
						}else if(tagResp.getName().contains("CTDA3")){
							if(!mapCTD3.containsKey(dtpDto.getTimeStamp())){
								mapCTD3.put(dtpDto.getTimeStamp(), dtpDto);
							}
						}
						else if(tagResp.getName().contains("CTDA1")){
							if(!mapCTDA1.containsKey(dtpDto.getTimeStamp())){
								mapCTDA1.put(dtpDto.getTimeStamp(), dtpDto);
							}
						}
						
						if(dtpDto.getValue()>=100){
						if(map.containsKey(tagResp.getName())){
						dtoDtpList.add(dtpDto);
						map.put(tagResp.getName(), dtpDto);
						}else{
							if(mapLess.containsKey(tagResp.getName())){
								dtoDtpListLess.add(dtpDto);
								mapLess.put(tagResp.getName(), dtpDto);
							}else{
								dtoDtpList.add(dtpDto);
								map.put(tagResp.getName(), dtpDto);
							}
						}}else{
							if(map.containsKey(tagResp.getName())){
								dtoDtpList.add(dtpDto);
								map.put(tagResp.getName(), dtpDto);
							}else{
							dtoDtpListLess.add(dtpDto);
							mapLess.put(tagResp.getName(), dtpDto);
							}
						}
						tms = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(dtp.getTimestamp());
					}
					dto.setDatapoints(dtoDtpList);
					
				}
        		dtoList.add(dto);
        	}
            
            dto =  new ResponseDTO();
            dtoDtpList = new ArrayList<>();
    		dto.setTag("CTIF1 - CTIF2");
            for (DatapointDTO dtoPointF1 : mapCTF1.values()) {
            	DatapointDTO dtoAux = mapCTF2.get(dtoPointF1.getTimeStamp());
            	if(dtoAux!=null){
            		dtoPointF1.setValue(dtoPointF1.getValue() - dtoAux.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}else{
            		dtoPointF1.setValue(dtoPointF1.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}
            }

            dto.setDatapoints(dtoDtpList);
//            dtoList.add(dto);
           
            
            dto =  new ResponseDTO();
            dtoDtpList = new ArrayList<>();
    		dto.setTag("CTDA2 - CTDA3");
            for (DatapointDTO dtoPointF1 : mapCTD2.values()) {
            	DatapointDTO dtoAux = mapCTD3.get(dtoPointF1.getTimeStamp());
            	if(dtoAux!=null){
            		dtoPointF1.setValue(dtoPointF1.getValue() - dtoAux.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}else{
            		dtoPointF1.setValue(dtoPointF1.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}
            }dto.setDatapoints(dtoDtpList);
//            dtoList.add(dto);
            
            dto =  new ResponseDTO();
            dtoDtpList = new ArrayList<>();
    		dto.setTag("CTDA1 - CTDA3");
            for (DatapointDTO dtoPointF1 : mapCTDA1.values()) {
            	DatapointDTO dtoAux = mapCTD3.get(dtoPointF1.getTimeStamp());
            	if(dtoAux!=null){
            		dtoPointF1.setValue(dtoPointF1.getValue() - dtoAux.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}else{
            		dtoPointF1.setValue(dtoPointF1.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}
            }dto.setDatapoints(dtoDtpList);
//            dtoList.add(dto);
        	
        	
        	tenant = null;
        	builder = null;
        	response = null;
        	tms = null;
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
	 * @param params
	 * @return -
	 */
	public List<ResponseDTO> queryTSOrdered(QueryParamsDTO params) {
		try {
			
			String	authorization = null;
        	List<Header> lHead = restClient.getSecureTokenForClientId();
        	for (Header header : lHead) {
        		authorization = header.getValue();
			}
        	TenantContext tenant = TenantContextFactory.createQueryTenantContextFromProvidedProperties
        			("https://time-series-store-predix.run.aws-usw02-pr.ice.predix.io/v1/datapoints",
        					authorization,this.PZID,this.tsInjectionWSConfig.getZoneId());
        QueryBuilder builder = null;
        if(params.getIntervalValue()==0){
        	builder = QueryBuilder.createQuery()
        	        .withStartAbs(params.getStart().getTime())
        	        .withEndAbs(params.getEnd().getTime())
        	        .addTags(QueryTag.Builder.createQueryTag().withTagNames(params.getTagsArray()).withLimit(100000)
        	        		.addAggregation(Aggregation.Builder.averageWithInterval(1, TimeUnit.MINUTES))
        	                        .build());
	    }else{
        builder = QueryBuilder.createQuery()
            	        .withStartAbs(params.getStart().getTime())
            	        .withEndAbs(params.getEnd().getTime())
            	        .addTags(QueryTag.Builder.createQueryTag().withTagNames(params.getTagsArray()).withLimit(100000)
            	        		.addAggregation(Aggregation.Builder.interpolateWithInterval(params.getIntervalValue(), TimeUnit.MINUTES))
            	                        .build());
		}
        	QueryResponse response = ClientFactory.queryClientForTenant(tenant).queryAll(builder.build());
        	String tms = null;
        	
        	ResponseDTO dto = new ResponseDTO();
        	DatapointDTO dtpDto = new DatapointDTO();
        	List<DatapointDTO> dtoDtpList = new ArrayList<>();
        	List<DatapointDTO> dtoDtpListLess = new ArrayList<>();
        	List<ResponseDTO> dtoList = new ArrayList<>();
        	TreeMap<Timestamp,DatapointDTO> mapCTF1 = new TreeMap<>();
    		TreeMap<Timestamp,DatapointDTO> mapCTF2 = new TreeMap<>();
    		TreeMap<Timestamp,DatapointDTO> mapCTD2 = new TreeMap<>();
    		TreeMap<Timestamp,DatapointDTO> mapCTD3 = new TreeMap<>();
    		TreeMap<Timestamp,DatapointDTO> mapCTDA1 = new TreeMap<>();
            for (TagResponse tagResp : response.getTags()) {
        		dto =  new ResponseDTO();
        		dto.setTag(tagResp.getName());
        		dtoDtpList = new ArrayList<>();
        		dtoDtpListLess = new ArrayList<>();
        		TreeMap<String,DatapointDTO> map = new TreeMap<>();
        		TreeMap<String,DatapointDTO> mapLess = new TreeMap<>();
        		
        		for (Result reslt : tagResp.getResults()) {
					for (DataPoint dtp : reslt.getDataPoints()) {
						
						dtpDto = new DatapointDTO();
						dtpDto.setTimeStamp(new Timestamp(dtp.getTimestamp()));
						dtpDto.setQuality(dtp.getQuality().getName());
						dtpDto.setValue(new Double(dtp.getValue().toString()));
						if(tagResp.getName().contains("CTIF1")){
							if(!mapCTF1.containsKey(dtpDto.getTimeStamp())){
								mapCTF1.put(dtpDto.getTimeStamp(), dtpDto);
							}
						}else if(tagResp.getName().contains("CTIF2")){
							if(!mapCTF2.containsKey(dtpDto.getTimeStamp())){
								mapCTF2.put(dtpDto.getTimeStamp(), dtpDto);
							}
						}else if(tagResp.getName().contains("CTDA2")){
							if(!mapCTD2.containsKey(dtpDto.getTimeStamp())){
								mapCTD2.put(dtpDto.getTimeStamp(), dtpDto);
							}
						}else if(tagResp.getName().contains("CTDA3")){
							if(!mapCTD3.containsKey(dtpDto.getTimeStamp())){
								mapCTD3.put(dtpDto.getTimeStamp(), dtpDto);
							}
						}
						else if(tagResp.getName().contains("CTDA1")){
							if(!mapCTDA1.containsKey(dtpDto.getTimeStamp())){
								mapCTDA1.put(dtpDto.getTimeStamp(), dtpDto);
							}
						}
						
						if(dtpDto.getValue()>=100){
						if(map.containsKey(tagResp.getName())){
						dtoDtpList.add(dtpDto);
						map.put(tagResp.getName(), dtpDto);
						}else{
							if(mapLess.containsKey(tagResp.getName())){
								dtoDtpListLess.add(dtpDto);
								mapLess.put(tagResp.getName(), dtpDto);
							}else{
								dtoDtpList.add(dtpDto);
								map.put(tagResp.getName(), dtpDto);
							}
						}}else{
							if(map.containsKey(tagResp.getName())){
								dtoDtpList.add(dtpDto);
								map.put(tagResp.getName(), dtpDto);
							}else{
									dtoDtpListLess.add(dtpDto);
									mapLess.put(tagResp.getName(), dtpDto);
							}
						}
						tms = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(dtp.getTimestamp());
						
					}
					dto.setDatapoints(dtoDtpList);
					
				}
        		dtoList.add(dto);
        	}
            
            dto =  new ResponseDTO();
            dtoDtpList = new ArrayList<>();
    		dto.setTag("CTIF1 - CTIF2");
            for (DatapointDTO dtoPointF1 : mapCTF1.values()) {
            	DatapointDTO dtoAux = mapCTF2.get(dtoPointF1.getTimeStamp());
            	if(dtoAux!=null){
            		dtoPointF1.setValue(dtoPointF1.getValue() - dtoAux.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}else{
            		dtoPointF1.setValue(dtoPointF1.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}
            }
            dto.setDatapoints(dtoDtpList);
//            dtoList.add(dto);
            
            dto =  new ResponseDTO();
            dtoDtpList = new ArrayList<>();
    		dto.setTag("CTDA2 - CTDA3");
            for (DatapointDTO dtoPointF1 : mapCTD2.values()) {
            	DatapointDTO dtoAux = mapCTD3.get(dtoPointF1.getTimeStamp());
            	if(dtoAux!=null){
            		dtoPointF1.setValue(dtoPointF1.getValue() - dtoAux.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}else{
            		dtoPointF1.setValue(dtoPointF1.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}
            }dto.setDatapoints(dtoDtpList);
//            dtoList.add(dto);
            
            dto =  new ResponseDTO();
            dtoDtpList = new ArrayList<>();
    		dto.setTag("CTDA1 - CTDA3");
            for (DatapointDTO dtoPointF1 : mapCTDA1.values()) {
            	DatapointDTO dtoAux = mapCTD3.get(dtoPointF1.getTimeStamp());
            	if(dtoAux!=null){
            		dtoPointF1.setValue(dtoPointF1.getValue() - dtoAux.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}else{
            		dtoPointF1.setValue(dtoPointF1.getValue());
            		dtoDtpList.add(dtoPointF1);
            	}
            }dto.setDatapoints(dtoDtpList);
//            dtoList.add(dto);
        	
        	
        	tenant = null;
        	builder = null;
        	response = null;
        	tms = null;
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
	 * @param params
	 * @return -
	 */
	public List<ResponseDTO> queryTS(QueryParamsDTO params) {
		try {
			log.info("inside TS Query;"+this.tsInjectionWSConfig.getZoneId());
			String	authorization = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImxlZ2FjeS10b2tlbi1rZXkiLCJ0eXAiOiJKV1QifQ.eyJqdGkiOiIzYmY2ZjdkMWMzYzY0NWZhOTdlZTkwNTU5ZjE2ZGFkYyIsInN1YiI6IjNtNGlsQ2xpZW50Iiwic2NvcGUiOlsiY2xpZW50cy5yZWFkIiwiYWNzLnBvbGljaWVzLndyaXRlIiwidGltZXNlcmllcy56b25lcy41Y2FmYTY4Yi04OGRiLTRiODItOWIxMy03ZjE0ODhhZDZiOTEuaW5nZXN0IiwidWFhLnJlc291cmNlIiwidGltZXNlcmllcy56b25lcy41Y2FmYTY4Yi04OGRiLTRiODItOWIxMy03ZjE0ODhhZDZiOTEudXNlciIsImFjcy5hdHRyaWJ1dGVzLndyaXRlIiwic2NpbS5yZWFkIiwiYWNzLnBvbGljaWVzLnJlYWQiLCJhY3MuYXR0cmlidXRlcy5yZWFkIiwicHJlZGl4LWFjcy10cmFpbmluZy56b25lcy41MTA5NTJlNi0wODI2LTQ1ZjktODcwOS00MWFlMzIxOGIxZTIudXNlciIsImNsaWVudHMud3JpdGUiLCJ0aW1lc2VyaWVzLnpvbmVzLjVjYWZhNjhiLTg4ZGItNGI4Mi05YjEzLTdmMTQ4OGFkNmI5MS5xdWVyeSIsInByZWRpeC1hc3NldC56b25lcy5jZmM0YzRlYy0xNWJkLTQ3YTgtYTQxNS01MDA0ZDVjNzZkNGYudXNlciIsImlkcHMucmVhZCIsInNjaW0ud3JpdGUiXSwiY2xpZW50X2lkIjoiM200aWxDbGllbnQiLCJjaWQiOiIzbTRpbENsaWVudCIsImF6cCI6IjNtNGlsQ2xpZW50IiwiZ3JhbnRfdHlwZSI6ImNsaWVudF9jcmVkZW50aWFscyIsInJldl9zaWciOiJmMmUwN2VmMyIsImlhdCI6MTQ5NzAzNjk0NiwiZXhwIjoxNDk3MDgwMTQ2LCJpc3MiOiJodHRwczovL2M2NWE5NzQ2LTM3ZTYtNDBkZi04NzMyLTRhYTQxZmQ0MWIyOS5wcmVkaXgtdWFhLnJ1bi5hd3MtdXN3MDItcHIuaWNlLnByZWRpeC5pby9vYXV0aC90b2tlbiIsInppZCI6ImM2NWE5NzQ2LTM3ZTYtNDBkZi04NzMyLTRhYTQxZmQ0MWIyOSIsImF1ZCI6WyJzY2ltIiwiY2xpZW50cyIsImFjcy5hdHRyaWJ1dGVzIiwidWFhIiwidGltZXNlcmllcy56b25lcy41Y2FmYTY4Yi04OGRiLTRiODItOWIxMy03ZjE0ODhhZDZiOTEiLCJwcmVkaXgtYXNzZXQuem9uZXMuY2ZjNGM0ZWMtMTViZC00N2E4LWE0MTUtNTAwNGQ1Yzc2ZDRmIiwiM200aWxDbGllbnQiLCJhY3MucG9saWNpZXMiLCJwcmVkaXgtYWNzLXRyYWluaW5nLnpvbmVzLjUxMDk1MmU2LTA4MjYtNDVmOS04NzA5LTQxYWUzMjE4YjFlMiIsImlkcHMiXX0.Whfy1e1Q7yFs4bVrsi7_eyBecnKGt7RNfikK2Gwj-M2ixDWzQDXf6xvWGdRPEmDFKDkng0sxuTFbBs3hv_9bBh2b39KeKe96FGLBm-pT9QIp4z_B6kzBbeW9aQt5kTGKTnwT3UaKZBxsr-gYFkH-dbDBYvCw36PiYR_8exXg3NKF7-W0LjqBx9-BND4d8iXjltvoOe4YMhPRIdyTS4tIQypauxn9TCnLYG5fvp9Wpv1N2Z3DMnIvuc7jZg0P9Rm1hK4RVO9z3MeIpxYKUVgaww5M1OHJ4N5mvJpaGtien0xTQBqNaOGAag-8-4spW2nTEo3yuK4KDIcEReFdrC9smA";
//        	List<Header> lHead = restClient.getSecureTokenForClientId();
//        	for (Header header : lHead) {
//        		authorization = header.getValue();
//			}
        	TenantContext tenant = TenantContextFactory.createQueryTenantContextFromProvidedProperties
        			("https://time-series-store-predix.run.aws-usw02-pr.ice.predix.io/v1/datapoints",
        					authorization,this.PZID,this.tsInjectionWSConfig.getZoneId());
        QueryBuilder builder = null;
        
        	builder = QueryBuilder.createQuery()
        	        .withStartAbs(params.getStart().getTime())
        	        .withEndAbs(params.getEnd().getTime())
        	        .addTags(QueryTag.Builder.createQueryTag().withTagNames(params.getTagsArray()).withLimit(100000)
        	        		.addAggregation(Aggregation.Builder.averageWithInterval(1, TimeUnit.MINUTES))
        	                        .build());
	    	
        	QueryResponse response = ClientFactory.queryClientForTenant(tenant).queryAll(builder.build());
        	ResponseDTO dto = new ResponseDTO();
        	DatapointDTO dtpDto = new DatapointDTO();
        	List<DatapointDTO> dtoDtpList = new ArrayList<>();
        	List<ResponseDTO> dtoList = new ArrayList<>();
        	if(response!=null)
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
					}
					dto.setDatapoints(dtoDtpList);
				}
        		dtoList.add(dto);
        	}else{
        		tenant = null;
            	builder = null;
            	response = null;
        		return null;
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

	
	
	
	

}
