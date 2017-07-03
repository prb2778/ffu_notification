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
public class TagStatusDTO {
	
	private String name = null;
	private String errorStatus = null;
	private String tool = null;
	private String area = null;
	
	
	/**
	 * @return the area
	 */
	public String getArea() {
		return this.area;
	}
	/**
	 * @param area the area to set
	 */
	public void setArea(String area) {
		this.area = area;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the errorStatus
	 */
	public String getErrorStatus() {
		return this.errorStatus;
	}
	/**
	 * @param errorStatus the errorStatus to set
	 */
	public void setErrorStatus(String errorStatus) {
		this.errorStatus = errorStatus;
	}
	/**
	 * @return the tool
	 */
	public String getTool() {
		return this.tool;
	}
	/**
	 * @param tool the tool to set
	 */
	public void setTool(String tool) {
		this.tool = tool;
	}
	
		

}
