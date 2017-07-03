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
public class FanParamsDTO {
	
	private String gateway = null;
	private String fan = null;
	/**
	 * @return the gateway
	 */
	public String getGateway() {
		return this.gateway;
	}
	/**
	 * @param gateway the gateway to set
	 */
	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
	/**
	 * @return the fan
	 */
	public String getFan() {
		return this.fan;
	}
	/**
	 * @param fan the fan to set
	 */
	public void setFan(String fan) {
		this.fan = fan;
	}
	
	

}
