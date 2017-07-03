package com.ge.predix.solsvc.training.ingestion.data_ingestion.type;

import java.util.Date;


public class CargoShelfLife {
	
	private String assetId;
	
	private long timestamp = new Date().getTime();
	
	private long shelfLife;
	

	private long id;

	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getShelfLife() {
		return shelfLife;
	}

	public void setShelfLife(long shelfLife) {
		this.shelfLife = shelfLife;
	}
	
	
	

}
