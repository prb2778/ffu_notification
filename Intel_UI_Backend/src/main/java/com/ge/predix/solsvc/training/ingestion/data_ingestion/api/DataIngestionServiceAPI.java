package com.ge.predix.solsvc.training.ingestion.data_ingestion.api;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ge.predix.solsvc.training.ingestion.data_ingestion.dto.ParamsDTO;

/**
 * 
 * @author predix -
 */
public interface DataIngestionServiceAPI
{
    /**
     * 
     * @param params Object -
     * @return ResponseBody Object -
     */
    public @ResponseBody String ingestTS(@RequestBody List<ParamsDTO> params);
	
    /**
     * 
     * 
     */
    public void startMonitorTS();

	/**
	 * @return -
	 */
	public String ingestFiles();

   

}
