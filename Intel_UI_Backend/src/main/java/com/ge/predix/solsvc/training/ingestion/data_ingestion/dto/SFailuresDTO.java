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
public class SFailuresDTO {
	
	private List<FailuresStsDTO> softFailures = new ArrayList<>();

	/**
	 * @return the softFailures
	 */
	public List<FailuresStsDTO> getSoftFailures() {
		return this.softFailures;
	}

	/**
	 * @param softFailures the softFailures to set
	 */
	public void setSoftFailures(List<FailuresStsDTO> softFailures) {
		this.softFailures = softFailures;
	}
	
	

}
