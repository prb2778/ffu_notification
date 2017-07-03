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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author predix -
 */
public class TagsDTO {
	
	private List<String> results = new ArrayList<>();

	/**
	 * @return the results
	 */
	public List<String> getResults() {
		return this.results;
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(List<String> results) {
		this.results = results;
	}
	
	

}
