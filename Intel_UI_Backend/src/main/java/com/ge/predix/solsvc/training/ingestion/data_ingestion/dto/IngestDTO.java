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

import java.sql.Timestamp;

/**
 * 
 * @author predix -
 */
public class IngestDTO {
	
	private Timestamp timestamp = null;
	private String tagId = null;
	private Double value = null;
	private int quality = 3;
	
	
	/**
	 * @return the timestamp
	 */
	public Timestamp getTimestamp() {
		return this.timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
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
	/**
	 * @return the value
	 */
	public Double getValue() {
		return this.value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Double value) {
		this.value = value;
	}
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
	
	

}
