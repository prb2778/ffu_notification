/*
 * Copyright (c) 2017 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */
 
package com.ge.predix.solsvc.training.ingestion.data_ingestion.dto;

import java.util.List;

/**
 * 
 * @author predix -
 */
public class ResponseDTO {
	
	private String tag = null;
	private List<DatapointDTO> datapoints = null;
	
	
	/**
	 * @return the tag
	 */
	public String getTag() {
		return this.tag;
	}
	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}
	/**
	 * @return the datapoints
	 */
	public List<DatapointDTO> getDatapoints() {
		return this.datapoints;
	}
	/**
	 * @param datapoints the datapoints to set
	 */
	public void setDatapoints(List<DatapointDTO> datapoints) {
		this.datapoints = datapoints;
	}
	
	

}
