package com.ge.predix.solsvc.training.ingestion.data_ingestion.handler;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ge.predix.solsvc.bootstrap.ams.dto.Asset;
import com.ge.predix.solsvc.bootstrap.ams.factories.AssetFactory;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.AdresseeDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.FanParamsDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.IngestDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.ParamsDTO;
import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.SimpleResponseDTO;

/**
 * 
 * @author predix -
 */
@Component
public class DataIngestionHandler
{
    private static Logger          log = Logger.getLogger(DataHandler.class);
    @Autowired
    private AssetFactory           assetFactory;

    @Autowired
    private TimeSeriesDataIngestionHandler timeSeriesDataIngestionHandler;

    /**
     * 
     */
    @Autowired
    private AssetDataHandler     assetDataHandler;

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
     * @param tenantId -
     * @param controllerId -
     * @param data -
     * @param authorization -
     * @return -
     */
    @SuppressWarnings("nls")
    public String handleData(String tenantId, String controllerId, String data, String authorization)
    {
        this.timeSeriesDataIngestionHandler.handleData(tenantId, controllerId, data, authorization);
        return "SUCCESS";
    }

    /**
     * @return -
     */
    public String listAssets()
    {
        List<Asset> assets = this.assetDataHandler.getAllAssets();
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        try
        {
            mapper.writeValue(writer, assets);
        }
        catch (JsonGenerationException e)
        {
            throw new RuntimeException(e);
        }
        catch (JsonMappingException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    /**
     * @return -
     */
    @SuppressWarnings("nls")
    public String retrieveAsset(String tag)
    {
      return this.assetDataHandler.retrieveAsset(null, "cargox" , tag,null);
       
       
    }

	/**
	 * @param params
	 * @return -
	 */
	public String ingestTS(List<ParamsDTO> params) {
		return this.timeSeriesDataIngestionHandler.ingestTS(params);
	}

	/**
	 * @param ingestList -
	 */
	public void ingestFilesIntoTS(List<IngestDTO> ingestList) {
		 this.timeSeriesDataIngestionHandler.ingestFilesIntoTS(ingestList);
	}

	/**
	 * @param sensor
	 * @param authorization 
	 * @return -
	 */
	public SimpleResponseDTO resetSensor(FanParamsDTO sensor, String authorization) {
		return this.timeSeriesDataIngestionHandler.resetSensor(sensor,authorization);
	}

	/**
	 * @param param
	 * @return -
	 */
	public String setAdressee(AdresseeDTO param) {
		return this.timeSeriesDataIngestionHandler.setAdressee(param);
	}
}
