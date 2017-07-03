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
public class DatapointDTO {
	
	private Timestamp timeStamp = null;
	private double value = 0;
	private String quality = null;
	/**
	 * @return the timeStamp
	 */
	public Timestamp getTimeStamp() {
		return this.timeStamp;
	}
	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}
	/**
	 * @return the value
	 */
	public double getValue() {
		return this.value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}
	/**
	 * @return the quality
	 */
	public String getQuality() {
		return this.quality;
	}
	/**
	 * @param quality the quality to set
	 */
	public void setQuality(String quality) {
		this.quality = quality;
	}
	
	

}
