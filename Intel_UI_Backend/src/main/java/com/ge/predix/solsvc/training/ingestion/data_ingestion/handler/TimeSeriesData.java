package com.ge.predix.solsvc.training.ingestion.data_ingestion.handler;


public class TimeSeriesData
{
    private String messageId;
    
    private String tagName;
    
    private String measureValue;  
    
    private String assetId;
    
    private long timeStamp = System.currentTimeMillis();
    
    /**
     * @return the messageId
     */
    public String getMessageId()
    {
        return this.messageId;
    }

    /**
     * @param messageId the messageId to set
     */
    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    /**
     * @return the tagName
     */
    public String getTagName()
    {
        return this.tagName;
    }

    /**
     * @param tagName the tagName to set
     */
    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }

    /**
     * @return the measureValue
     */
    public String getMeasureValue()
    {
        return this.measureValue;
    }

    /**
     * @param measureValue the measureValue to set
     */
    public void setMeasureValue(String measureValue)
    {
        this.measureValue = measureValue;
    }

    
    
    public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	/**
     * @return the timeStamp
     */
    public long getTimeStamp()
    {
        return this.timeStamp;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

	public TimeSeriesData(String messageId, String tagName,
			String measureValue, String assetId) {
		super();
		this.messageId = messageId;
		this.tagName = tagName;
		this.measureValue = measureValue;
		this.assetId = assetId;
	}

	@Override
	public String toString() {
		return "TimeSeriesData [messageId=" + messageId + ", tagName="
				+ tagName + ", measureValue=" + measureValue + ", assetId="
				+ assetId + ", timeStamp=" + timeStamp + "]";
	}


    
    
  
}
