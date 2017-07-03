""" 
AUTHORS: Patrick Bean + Jeffrey Lentz
COMPANY: GENERAL ELECTRIC, DIGITAL (GE DIGITAL)
DATE: 6/01/2017

TITLE:-------------Predix Timeseries Query

DESCRIPTION:--------THIS PROGRAM generate Querie responses from Predix Timeseries and saves results to csv files.

"""
import requests
import json
import pandas as pd

         
        
def getPredixTSData(QueryParams):
        global parameters       
        global CertFilePath
        global UnitListPath
        global TagListPath
        global frame
        global reslist
        
        parameters = QueryParams
        ########## CONFIGURATION PARAMETERS ##############
        # Path to GE External Certificates
        # Unit and Tag List files

        # row limit for query results

        #sys.path.append(r'C:\Users\212547157\workspace\CassandraQuery')        
        ############### Get Tags and Units Lists ####
        def get_tag_list():
            TagresList = json.loads(parameters['tag'])
            return TagresList
               
        def get_limit():
            limit = parameters["limit"]
            return limit        
        def get_startdate():
            startdate = str(parameters["starttime"])
            return startdate            
        def get_enddate():
            end_date = str(parameters["endtime"])
            return end_date 
        def get_bearer_token():
            beartoken = str(parameters["bearer_token"])   
            return beartoken        
        def get_TS_URL():
            tsURL = str(parameters['Timeseries_URL'])
            return tsURL
        def get_zone_id():
            zone = str(parameters['zone-id'])
            return zone
        ############### Get Tags and Units Lists ####        
        def query_cassandra(tag, from_date, to_date, limit, bearer_token, URL, tszone):   
            print(URL)      
            url = URL + "v1/datapoints"

            limit = str(int(limit))
            payload = r'{"start": ' + from_date + ', "end": ' + to_date + ', "tags": [{"name": "' + tag + '", "limit": ' + limit + ', "order": "desc"}]}'
            headers = {
                'predix-zone-id': tszone,
                'content-type': "application/json",
                'authorization': "Bearer " + bearer_token,
                'cache-control': "no-cache"
                }             
            response = requests.request("POST", url, data=payload, headers=headers)                         
            data = response.json()               
            data_df = pd.DataFrame(data['tags'][0]['results'][0]['values'])
            data_df.columns = ['Timestamp', 'Value', 'Quality']
            data_df['Tagname'] = data['tags'][0]['name']
            data_df['Timestamp'] = pd.to_datetime(data_df['Timestamp']*1000000)               
            return data_df    
                    # Call to function for retrieving token        
        bearer_token = get_bearer_token()      
        tag_list = get_tag_list()        
        limit = get_limit()        
        # Call to function for selecting Start + End Dates
        from_date= get_startdate()
        to_date = get_enddate()
        TS_URL = get_TS_URL()
        ZoneID = get_zone_id()
        print("Tag DATA FROM: ", from_date, " TO ", to_date, " WILL BE Queried from Casandra")        
        ########### Start Query Loop ###############
        ##### Loop through selected Tags + Selected OSMs and send query--########
        #####-- to the cloud for selected Dates and write results to CSV ########
        AllResults = {}   
        for tag in tag_list:               
            CloudResult = query_cassandra(tag, from_date, to_date, limit, bearer_token, TS_URL, ZoneID)
            AllResults[tag] = CloudResult                                                                   
        return AllResults   
        
        
        
def getTaglist(bearer_token, TS_URL, zone):
    
    print("!!!! The ZoneID is " + zone)
    print("!!!! The ZoneID is " + TS_URL)
    url = TS_URL + "v1/tags"   
    headers = {
        'authorization': "bearer " + bearer_token,
        'predix-zone-id': zone,
        'content-type': "application/json",
        'cache-control': "no-cache"
        }
    
    print("Please Hold -- Building TagList from Predix Timeseries")
    response = requests.request("GET", url, headers=headers)
    jsonResponse = json.loads(response.text)
    print("!!!!!!!!!! THIS IS THE TAG LIST: " + json.dumps(jsonResponse))
    
    return jsonResponse
        ############# END Query Loop ###################
                
        

    
    
