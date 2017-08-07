""" 
AUTHORS: Patrick Bean + Jeffrey Lentz
COMPANY: GENERAL ELECTRIC, DIGITAL (GE DIGITAL)
DATE: 6/01/2017

TITLE:-------------Predix Timeseries Query

DESCRIPTION:--------THIS PROGRAM generate Querie responses from Predix Timeseries and saves results to csv files.

"""

class getPredixTSData():
    def __init__(self):
        from calwidget import Calendar
        import sys
        import calendar
        from tkinter import ttk
        import tkinter
        import requests
        import pandas as pd
        import datetime 
        import time
        import OSMTagSelectV3
        global CertFilePath
        global UnitListPath
        global TagListPath
        ########## CONFIGURATION PARAMETERS ##############
        # Path to GE External Certificates
        CertFilePath = r"C:\Certificates\GE_External_Certificate2.pem"
        # Unit and Tag List files
        UnitListPath = 'Aero_unit_list.csv'
        TagListPath = 'taglist.csv'
        # row limit for query results
        limit = 1000000
        #sys.path.append(r'C:\Users\212547157\workspace\CassandraQuery')
        
        
        ###### Pick Dates for Query ######
        def ChooseDates():  
            root = tkinter.Tk()
            root.title('Ttk Calendar')
            ttkcal = Calendar(firstweekday=calendar.SUNDAY)
            ttkcal.pack(expand=1, fill='both')
            if 'win' not in sys.platform:
                style = ttk.Style()
                style.theme_use('clam')
            def print_date():
                print("Selected Date", ttkcal.selection, " CLOSE CALANDER NOW") #THIS IS OUTPUT TO THE CONSOLE WHENEVER A DATE IS SELECTED
                return(ttkcal.selection)    
            tkinter.Button(root, text="CLICK HERE & CLOSE WINDOW", command=print_date).pack() #THE CALANDER TEXT CAN BE EDITED HERE
            root.mainloop()
            return(ttkcal.selection)
        ############### End Pick Dates ############
        
        ############### Get Tags and Units Lists ####
        def get_tag_list(bearer_token, unit_list):
            tag_list = OSMTagSelectV3.getTagList(bearer_token, unit_list)
            return tag_list
        
        def get_unit_list(UntiListPath):
            unit_list = OSMTagSelectV3.getOSMList(UnitListPath)
            return unit_list
        
        
        ############### Get Tags and Units Lists ####
        
        def query_cassandra(tag, from_date, to_date, limit, bearer_token):
            data_df = None
            try:
                url = "https://time-series-ingress.dev.gepowerpredix.com/v1/datapoints"
                from_date = str(int(time.mktime(from_date.timetuple()) * 1000))
                to_date = str(int(time.mktime(to_date.timetuple()) * 1000))
                limit = str(int(limit))
                payload = r'{"start": ' + from_date + ', "end": ' + to_date + ', "tags": [{"name": "' + tag + '", "limit": ' + limit + ', "order": "desc"}]}'
                headers = {
                    'predix-zone-id': "385c05f9-9d4c-43a8-ab3e-7693ec4d50b6",
                    'content-type': "text/plain",
                    'authorization': "Bearer " + bearer_token,
                    'cache-control': "no-cache",
                    'postman-token': "8a69bb9f-8505-429f-f954-c8a191973cbb"
                    }      
                proxies = {
                    'http': 'http://iss-americas-pitc-alpharetta.corporate.ge.com:80',
                    'https': 'http://iss-americas-pitc-alpharetta.corporate.ge.com:80',
                }        
                response = requests.request("POST", url, data=payload, headers=headers, proxies = proxies, verify = CertFilePath)       
                data = response.json()
                data_df = pd.DataFrame(data['tags'][0]['results'][0]['values'])
                data_df.columns = ['Timestamp', 'Value', 'Quality']
                data_df['Tagname'] = data['tags'][0]['name']
                data_df['Timestamp'] = pd.to_datetime(data_df['Timestamp'], unit='ms')
            except:
                print('caught error during query to Cassandra')
                raise
            return data_df
        
        
        def getTaglist(bearer_token):
            url = "https://time-series-ingress.dev.gepowerpredix.com/v1/tags"
            
            headers = {
                'content-type': "application/json",
                'authorization': "Bearer "+ bearer_token, 
                'predix-zone-id': "385c05f9-9d4c-43a8-ab3e-7693ec4d50b6",
                'cache-control': "no-cache",
                'postman-token': "fd32d7d6-4d33-ec05-76e9-ceb8a5ce00f7"
                }
            
            response = requests.request("GET", url, headers=headers)
            
            print(response.text)
        
        
        def get_bearer_token():
            try:
                url = "https://f6d0524d-28d1-4af8-a21c-3c779790aff4.predix-uaa.run.aws-usw02-pr.ice.predix.io/oauth/token"
        
                querystring = {"grant_type": "client_credentials"}
        
                payload = "grant_type=client_credentials"
                headers = {
                    'content-type': "application/x-www-form-urlencoded",
                    'authorization': "Basic cG93ZXJzaXRfdHM6R3VoRkhROFZFYW4ycnFGcmNFYXQ3ZmRx",
                    'cache-control': "no-cache",
                    'postman-token': "55caa4ce-0f46-d1f2-1e4e-0f28f11034e5"
                }
                proxies = {
                    'http': 'http://proxy-privzen-src.research.ge.com:80',
                    'https': 'http://proxy-privzen-src.research.ge.com:80',
                }
                response = requests.request("POST", url, data=payload, headers=headers, params=querystring, proxies=proxies)
                bearer_token = response.json()['access_token']
                text_file = open("bearer_token.txt", "w")
                text_file.write(bearer_token)
                text_file.close()
            except:
                bearer_token = open("bearer_token.txt", "r").read()
            return bearer_token
           
        # Call to function for retrieving token
        try:
            bearer_token = get_bearer_token()
        except:
            print("Cant get Bearer Token")
            
        # Call to function for building unit list
        
        unit_list = get_unit_list(UnitListPath)
        
        # Call to function for building unit list
        try:
            tag_list = get_tag_list(bearer_token, unit_list)
        except:
            print("Cant Build Tag List from Cloud")
        
        
        # Call to function for selecting Start + End Dates
        from_date = ChooseDates()
        to_date = ChooseDates() 
        print("Tag DATA FROM: ", from_date, " TO ", to_date, " WILL BE Queried from Casandra")
        
        ########### Start Query Loop ###############
        ##### Loop through selected Tags + Selected OSMs and send query--########
        #####-- to the cloud for selected Dates and write results to CSV ########
        AllResults = {}
        for esn in unit_list:
            OSMResults = {}
        
            ESNosm = esn.split("__")   
            osm = ESNosm[1]
            cnt = 0
            for tag in tag_list:
                try:
                    CloudResult = query_cassandra(tag, from_date, to_date, limit, bearer_token)
                    OSMResults[osm + "__" + tag] = CloudResult  
                    AllResults[osm + "__" + tag] = CloudResult                
                except:
                    print("OSM " + osm + " Has No Data For Tag: " + tag + " For Dates: " + str(from_date) + "  -  " + str(to_date))
                    continue                                  
            with open(('%s'+"_" + datetime.datetime.now().strftime('%Y_%m_%d_%H_%M') + '.csv') % esn, 'w+') as f: 
                for key, value in OSMResults.items():
                    if len(value)>0 and cnt == 0:
                        value.to_csv(f,index=False)
                        cnt = cnt+1
                    elif len(value)>0 and cnt > 0:
                        value.to_csv(f,index=False, header=None)          
                f.close()        
        print("Data Extraction Complete")
        ############# END Query Loop ###################