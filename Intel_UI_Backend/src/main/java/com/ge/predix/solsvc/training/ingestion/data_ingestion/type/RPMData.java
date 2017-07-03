
package com.ge.predix.solsvc.training.ingestion.data_ingestion.type;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class RPMData {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("rpm")
    @Expose
    private Double rpm;

    /**
     * 
     * @return
     *     The name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The rpm
     */
    public Double getRpm() {
        return rpm;
    }

    /**
     * 
     * @param rpm
     *     The rpm
     */
    public void setRpm(Double rpm) {
        this.rpm = rpm;
    }

}
