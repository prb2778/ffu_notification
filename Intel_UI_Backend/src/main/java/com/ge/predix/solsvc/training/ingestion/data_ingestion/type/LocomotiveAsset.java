package com.ge.predix.solsvc.training.ingestion.data_ingestion.type;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class LocomotiveAsset {

@SerializedName("uri")
@Expose
private String uri;
@SerializedName("id")
@Expose
private String id;
@SerializedName("carrier")
@Expose
private String carrier;
@SerializedName("customer")
@Expose
private String customer;
@SerializedName("itemshipped")
@Expose
private String itemshipped;
@SerializedName("source")
@Expose
private String source;
@SerializedName("destination")
@Expose
private String destination;
@SerializedName("shipmentDate")
@Expose
private String shipmentDate;
@SerializedName("temp_min")
@Expose
private String tempMin;
@SerializedName("temp_max")
@Expose
private String tempMax;
@SerializedName("humid_min")
@Expose
private String humidMin;
@SerializedName("humid_max")
@Expose
private String humidMax;
@SerializedName("data_point")
@Expose
private String dataPoint;

/**
*
* @return
* The uri
*/
public String getUri() {
return uri;
}

/**
*
* @param uri
* The uri
*/
public void setUri(String uri) {
this.uri = uri;
}

/**
*
* @return
* The id
*/
public String getId() {
return id;
}

/**
*
* @param id
* The id
*/
public void setId(String id) {
this.id = id;
}

/**
*
* @return
* The carrier
*/
public String getCarrier() {
return carrier;
}

/**
*
* @param carrier
* The carrier
*/
public void setCarrier(String carrier) {
this.carrier = carrier;
}

/**
*
* @return
* The customer
*/
public String getCustomer() {
return customer;
}

/**
*
* @param customer
* The customer
*/
public void setCustomer(String customer) {
this.customer = customer;
}

/**
*
* @return
* The itemshipped
*/
public String getItemshipped() {
return itemshipped;
}

/**
*
* @param itemshipped
* The itemshipped
*/
public void setItemshipped(String itemshipped) {
this.itemshipped = itemshipped;
}

/**
*
* @return
* The source
*/
public String getSource() {
return source;
}

/**
*
* @param source
* The source
*/
public void setSource(String source) {
this.source = source;
}

/**
*
* @return
* The destination
*/
public String getDestination() {
return destination;
}

/**
*
* @param destination
* The destination
*/
public void setDestination(String destination) {
this.destination = destination;
}

/**
*
* @return
* The shipmentDate
*/
public String getShipmentDate() {
return shipmentDate;
}

/**
*
* @param shipmentDate
* The shipmentDate
*/
public void setShipmentDate(String shipmentDate) {
this.shipmentDate = shipmentDate;
}

/**
*
* @return
* The tempMin
*/
public String getTempMin() {
return tempMin;
}

/**
*
* @param tempMin
* The temp_min
*/
public void setTempMin(String tempMin) {
this.tempMin = tempMin;
}

/**
*
* @return
* The tempMax
*/
public String getTempMax() {
return tempMax;
}

/**
*
* @param tempMax
* The temp_max
*/
public void setTempMax(String tempMax) {
this.tempMax = tempMax;
}

/**
*
* @return
* The humidMin
*/
public String getHumidMin() {
return humidMin;
}

/**
*
* @param humidMin
* The humid_min
*/
public void setHumidMin(String humidMin) {
this.humidMin = humidMin;
}

/**
*
* @return
* The humidMax
*/
public String getHumidMax() {
return humidMax;
}

/**
*
* @param humidMax
* The humid_max
*/
public void setHumidMax(String humidMax) {
this.humidMax = humidMax;
}

/**
*
* @return
* The dataPoint
*/
public String getDataPoint() {
return dataPoint;
}

/**
*
* @param dataPoint
* The data_point
*/
public void setDataPoint(String dataPoint) {
this.dataPoint = dataPoint;
}

}

