package com.ge.predix.solsvc.training.ingestion.data_ingestion.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ge.predix.solsvc.bootstrap.ams.dto.Asset;
import com.ge.predix.solsvc.bootstrap.ams.dto.AssetMeter;
import com.ge.predix.solsvc.bootstrap.ams.dto.MeterDatasource;
import com.ge.predix.solsvc.bootstrap.ams.factories.AssetFactory;

/**
 * 
 * @author predix -
 */
@Component
public class AssetDataHandler extends BaseFactoryIT
{
    private static Logger log          = Logger.getLogger(AssetDataHandler.class);
    @Autowired
    private AssetFactory  assetFactory;
    
    /**
     * @return -
     */
    public List<Asset> getAllAssets()
    {
        List<Header> headers = this.restClient.getOauthHttpHeaders(this.restConfig.getOauthClientId(),
                this.restConfig.getOauthClientIdEncode());
        List<Asset> assets = this.assetFactory.getAllAssets(headers);
        /*
         * if (assets != null) {
         * for (Asset asset:assets) {
         * log.info("Asset Name 				: "+asset.getAssetId());
         * log.info("Asset Uri 				: "+asset.getUri());
         * log.info("Asset Specification 	: "+asset.getSpecification()+"\n");
         * }
         * }
         */
        return assets;
    }

    /**
     * @param uuid -
     * @param filter -
     * @param value -
     * @param authorization -
     * @return -
     */
    public String retrieveAsset(String uuid, String filter, String value, String authorization)
    {
        List<Header> headers = null;
        if ( StringUtils.isEmpty(authorization) )
        {
        	headers=this.restClient.getSecureTokenForClientId();
        	//headers.add(new BasicHeader("Predix-Zone-Id","ac3caa34-7874-44b1-b2a8-d06e09b85844"));
        	headers.add(new BasicHeader("Predix-Zone-Id","7c39f4b1-142c-45be-acf0-7d8dcc13fdfd"));
          //  headers = this.restClient.getOauthHttpHeaders(this.restConfig.getOauthClientId(),
          //          this.restConfig.getOauthClientIdEncode());
            
        }
        else
        {
           headers = new ArrayList<Header>();
            this.restClient.addSecureTokenToHeaders(headers, authorization);
        }
        //this.restClient.addZoneToHeaders(headers, this.assetRestConfig.getZoneId());
        
        
        
        String asset = cargoAssetByTag(filter,value,headers);
       return asset;
    }
    
    
    public String cargoAssetByTag(String base,String tag, List<Header> headers){
    	CloseableHttpResponse response = null;
    	try { 
    	 response = getAssetbyTag(base,tag, headers);
        if ( response == null || response.getStatusLine() == null
                || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK )
        {
            throw new RuntimeException("invalid response" + response);
        }
        return EntityUtils.toString(response.getEntity());
    	 } catch (ParseException | IOException e)
        {
            try
            {
            	if( response == null ) 
            		throw new RuntimeException(e.getMessage(), e);
            	else 
            		throw new RuntimeException("json=" +  EntityUtils.toString(response.getEntity()) +e.getMessage(), e);
            }
            catch (ParseException | IOException e1)
            {
                throw new RuntimeException(e.getMessage(), e);
            }

        }
    	finally {
    		if (response!=null )
				try {
					response.close();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}	
    	}
        
    }
    
    public CloseableHttpResponse getAssetbyTag(String base, String value, List<Header> headers)
    {
    	String url=null;
    	
    	 // remove harding coding
    	if(value==null){
    		url = "https://predix-asset.run.aws-usw02-pr.ice.predix.io:443/" + base;
    	}else
    	{
    		url = "https://predix-asset.run.aws-usw02-pr.ice.predix.io:443/" + base + "/"+   value; //$NON-NLS-1$
    	}
        System.out.println(url);
        return this.restClient.get(url, headers);
    }

    /**
     * Lookup the Asset based on a Filter = Value and in the Asset find the matching Predix Machine MeterDataSource Node name.
     * 
     * @param filter -
     * @param value -
     * @param nodeName -
     * @param authorization -
     * @return -
     */
//    @SuppressWarnings("nls")
    /*public Map<String, AssetMeter> getTimeSeriesTag(String filter, String value, String nodeName, String authorization)
    {
        Map<String, AssetMeter> ret = new HashMap<String, AssetMeter>(1);
        for (int i = 0; i < this.retryCount; i++)
        {
            Asset asset = null;
            if ( asset != null )
            {
                LinkedHashMap<String, AssetMeter> meters = asset.getAssetMeter();
                if ( meters != null )
                {
                    for (Entry<String, AssetMeter> entry : meters.entrySet())
                    {
                        AssetMeter assetMeter = entry.getValue();
                        MeterDatasource dataSource = assetMeter.getMeterDatasource();
                        if ( dataSource != null && !dataSource.getNodeName().isEmpty()
                                && dataSource.getNodeName().equals(nodeName) )
                        {
                            ret.put(entry.getKey(), assetMeter);
                            return ret;
                        }
                    }
                    log.warn("2. asset has no assetmeters with matching nodeName, unable to find filter=" + filter
                            + " = " + value + " nodeName=" + nodeName + " authorization=" + authorization);
                }
                else
                    log.warn("3. asset has no meters, unable to find filter=" + filter + " = " + value + " nodeName="
                            + nodeName + " authorization=" + authorization);
            }
            else
                log.warn("4. asset not found, unable to find filter=" + filter + " = " + value + " nodeName="
                        + nodeName + " authorization=" + authorization);

        }
        return ret;
    }*/
}
