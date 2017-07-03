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
public class FailuresStsDTO {
	
	private String result = null;
	private String value = null;
	private String units = null;
	
	
	/**
	 * @return the result
	 */
	public String getResult() {
		return this.result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
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
	 * @return the units
	 */
	public String getUnits() {
		return this.units;
	}
	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	
	
	
}
