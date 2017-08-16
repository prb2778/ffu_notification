'''
Created on Jun 19, 2017

@author: 212547157
'''
from flask import Flask, request, jsonify
import requests
import os
import TimeSeriesQuery as getTS
import json
from flask_cors import CORS, cross_origin
import boto3
import datetime

#### BLOBSTORE Configuration DETAILS ####
global UAA_URL,timeseries_url, timeseries_zone, access_key, secret_key, bucket, blobHost, BASE64ENCODING


## SET this only for local testing, if VCAPS is set env they will be overwritten by VCAPS.
CLIENT_ID = None
#CLIENT_ID = "app_client_id"
UAA_URL=None 
#UAA_URL="https://61a46d35-834f-47fd-beea-5dd1c1c5bbec.predix-uaa.run.aws-usw02-pr.ice.predix.io" 
BASE64ENCODING =None 
#BASE64ENCODING ="S2VudC1Eb2pvLUNsaWVudDpJbnRlbERvam8="
#timeseries_zone = "ed578c7f-b571-49f7-bdd5-2b6fd87b961b"
#timeseries_url = "https://time-series-store-predix.run.aws-usw02-pr.ice.predix.io/"
port = int(os.getenv("PORT", 9099))
app = Flask(__name__)
cors = CORS(app)
## Setting up Oauth2 , this values should be read from vcaps .
APP_URL= None


# Get UAA credentials from VCAPS
if 'VCAP_SERVICES' in os.environ:
    services = json.loads(os.getenv('VCAP_SERVICES'))
    UAA_URL = services['predix-uaa'][0]['credentials']['uri']
    timeseries_zone = services['predix-timeseries'][0]["credentials"]['query']['zone-http-header-value']
    timeseries_url = services['predix-timeseries'][0]["credentials"]['query']['uri'].split("v1")[0]
    blobstore = services['predix-blobstore'][0]['credentials']
    access_key = blobstore["access_key_id"]
    secret_key = blobstore['secret_access_key']
    bucket = blobstore['bucket_name']
    blobHost = blobstore['host']

# Get UAA credentials from VCAPS
if 'VCAP_APPLICATION' in os.environ:
    applications = json.loads(os.getenv('VCAP_APPLICATION'))
    app_details_uri = applications['application_uris'][0]
    APP_URL = 'https://'+app_details_uri
else :
    APP_URL = "http://localhost:"+str(port)

if(os.getenv('client_id')):
    CLIENT_ID = os.getenv('client_id')

if(os.getenv('base64encodedClientDetails')):
    BASE64ENCODING = os.getenv('base64encodedClientDetails')

###########################################################################################################################

##########  START SECURE ENDPOINT ONE  ##############

@app.route('/tags', methods = ['GET'])
def getTaglist():
    

    print("retrieving tag list from cloud")
    dataresponse = {}           
    #token = request.headers.get('Authorization')#<--------- STEP-1, Get the Token.... 
    token = request.headers.get('Authorization')
    token = str(token).split(" ")[1]#<----STEP-2, Seperate the "Bearer" from the "Token" and return only the "Token"
    tokenCheck = check_token(token)#<----STEP-3, Check The Token and Return Expiration Time     
    if tokenCheck: #<------STEP-4, Verify Token is Valid (TUE / FALSE)   
        Btoken = get_bearer_token()
        dataresponse = getTS.getTaglist(Btoken, timeseries_url, timeseries_zone)
        print("taglist sent")                   
    else:            
        dataresponse['results'] = ['Please','Log into UAA',str(tokenCheck)]#<----STEP-4 (IF TOKEN IS EXPIRIED, SEND MESSAGE BACK TO USER)        
    return jsonify(dataresponse)


##########  END SECURE ENDPOINT ONE  ##############            

##########  START SECURE ENDPOINT TWO  ##############
@app.route('/datapoints', methods = ['POST'])
def getTSdata():   
    print("retrieving timeseries data")          
    token = request.headers.get('Authorization')#<--------- STEP-1, Get the Token.... 
    token = str(token).split(" ")[1]#<----STEP-2, Seperate the "Bearer" from the "Token" and return only the "Token"
    tokenCheck = check_token(token)#<----STEP-3, Check The Token and Return Expiration Time     
    if tokenCheck: #<------STEP-4, Verify Token is Valid (TUE / FALSE)    
        parameters = {
                   "starttime": json.dumps(request.json['starttime']),
                   "endtime": json.dumps(request.json['endtime']),
                   "tag": json.dumps(request.json['tag']),
                   "limit": json.dumps(request.json['limit']),
                   "bearer_token": get_bearer_token(),
                   "Timeseries_URL":  timeseries_url ,
                   "zone-id": timeseries_zone                                                
                  }   
        dataResponse = getTS.getPredixTSData(parameters)
        print("About to send data to BLOBSTORE!")
        upload_file(dataResponse)             
       
        print("Done uploading to Blobstore")
    else:
        print("Must Log In With UAA")
        
    return "Upload Complete"

######### ORIGINAL CODE ###   

    
##########  END SECURE ENDPOINT TWO  ##############

####################################################################################################################

##############################  TOKEN CHECKING ##################################

def check_token(token):

    url = UAA_URL + "/check_token"
    payload= "token=" + token
    headers = {
               'authorization': "Basic " + BASE64ENCODING,
               'content-type': "application/x-www-form-urlencoded"
    }
    response = requests.request("POST", url, data=payload, headers=headers)   
    token_json = response.json()
    print("Checked Token Details")
    print(url)
    print(token_json)
    if "error" in token_json:
        Authorized = False
    elif "exp" in token_json:
        Authorized = True
    else:
        Authorized = "Somethings Wrong"
   
    return Authorized

##############################  END TOKEN CHECKING ##################################

##########################################################################################################################################

def upload_file(datadict):  
    print("!!!!!!!!! ABOUT TO UPLOAD FILE")
    client = _get_s3_client()
    for key, value in datadict.items():                
        with open('fpath.csv',"w") as f:
            value.to_csv(f)
        f.close()
        if len(value)>0:
            client.upload_file('fpath.csv', bucket, key + "_"+datetime.datetime.now().strftime('%Y_%m_%d_%H_%M')+".csv")                             
        os.remove('fpath.csv')        
    #return 'Tag_Data_'+ datetime.datetime.now().strftime('%Y_%m_%d_%H_%M')

def _get_s3_client():
    print("ABOUT TO CONNECT TO S3")
    awssession = boto3.session.Session(aws_access_key_id=access_key, aws_secret_access_key=secret_key)
    config = boto3.session.Config(signature_version='s3', s3={'addressing_style': 'virtual'})
    client = awssession.client('s3', endpoint_url="https://"+blobHost, config=config)
    return client


def get_bearer_token():
    print("FETCHING BEARER TOKEN")
    url = UAA_URL + "/oauth/token?grant_type=client_credentials"
    headers = {
        'authorization': "Basic " + BASE64ENCODING,
        'cache-control': "no-cache"
        }
    response = requests.request("POST", url, headers=headers)               
    bearer_token = response.json()['access_token']
    return bearer_token          

if __name__ == '__main__':   
    app.run(host= '0.0.0.0', port=port)



