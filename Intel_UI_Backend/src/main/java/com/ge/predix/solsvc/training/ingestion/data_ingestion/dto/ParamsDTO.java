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



/**
 * 
 * @author predix -
 */
public class ParamsDTO {
	
	private String timeStamp = null;
	private String value = null;
	private String tagId = null;
	private int quality = 3;
	
	
	
	
	
	/**
	 * @return the quality
	 */
	public int getQuality() {
		return this.quality;
	}
	/**
	 * @param quality the quality to set
	 */
	public void setQuality(int quality) {
		this.quality = quality;
	}
	/**
	 * @return the timeStamp
	 */
	public String getTimeStamp() {
		return this.timeStamp;
	}
	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the tagId
	 */
	public String getTagId() {
		return this.tagId;
	}
	/**
	 * @param tagId the tagId to set
	 */
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}
	
	
	
	
	
	

}
