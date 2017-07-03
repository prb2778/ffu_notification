
package com.ge.predix.solsvc.training.ingestion.data_ingestion.type;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class LocomotiveGatewayType {

    @SerializedName("rpmData")
    @Expose
    private RPMData rpmData;
    @SerializedName("currentTime")
    @Expose
    private Long currentTime;
    @SerializedName("torquedata")
    @Expose
    private TorqueData torquedata;
    @SerializedName("locdata")
    @Expose
    private LocationData locdata;

    public LocationData getLocdata() {
		return locdata;
	}

	public void setLocdata(LocationData locdata) {
		this.locdata = locdata;
	}

	/**
     * 
     * @return
     *     The hData
     */
    public RPMData getRpmData() {
        return rpmData;
    }

    /**
     * 
     * @param hData
     *     The h_data
     */
    public void setRpmData(RPMData rpmData) {
        this.rpmData = rpmData;
    }

    public TorqueData getTorquedata() {
		return torquedata;
	}

	public void setTorquedata(TorqueData torquedata) {
		this.torquedata = torquedata;
	}

	/**
     * 
     * @return
     *     The currentTime
     */
    public Long getCurrentTime() {
        return currentTime;
    }

    /**
     * 
     * @param currentTime
     *     The current_time
     */
    public void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }

    
   

}
