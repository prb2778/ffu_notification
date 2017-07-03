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
public class AdresseeDTO {
	
	private int failure = 0;
	private List<String> email = new ArrayList<>();
	/**
	 * @return the failure
	 */
	public int getFailure() {
		return this.failure;
	}
	/**
	 * @param failure the failure to set
	 */
	public void setFailure(int failure) {
		this.failure = failure;
	}
	/**
	 * @return the email
	 */
	public List<String> getEmail() {
		return this.email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(List<String> email) {
		this.email = email;
	}
	
	

}
