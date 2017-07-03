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
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author predix -
 */
public class QueryParamsDTO {
	
	private Timestamp start = null;
	private Timestamp end = null;
	private int intervalValue = 0;
	private List<String> tagsArray = new ArrayList<>();
	
	/**
	 * @return the start
	 */
	public Timestamp getStart() {
		return this.start;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(Timestamp start) {
		this.start = start;
	}
	/**
	 * @return the end
	 */
	public Timestamp getEnd() {
		return this.end;
	}
	/**
	 * @param end the end to set
	 */
	public void setEnd(Timestamp end) {
		this.end = end;
	}
	/**
	 * @return the intervalValue
	 */
	public int getIntervalValue() {
		return this.intervalValue;
	}
	/**
	 * @param intervalValue the intervalValue to set
	 */
	public void setIntervalValue(int intervalValue) {
		this.intervalValue = intervalValue;
	}
	/**
	 * @return the tagsArray
	 */
	public List<String> getTagsArray() {
		return this.tagsArray;
	}
	/**
	 * @param tagsArray the tagsArray to set
	 */
	public void setTagsArray(List<String> tagsArray) {
		this.tagsArray = tagsArray;
	}
	
	
}
