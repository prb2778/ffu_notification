""" 
AUTHOR: PATRICK BEAN
COMPANY: GENERAL ELECTRIC, DIGITAL (GE DIGITAL)
DATE: 6/01/2017

TITLE:------------- Tag & OSM Selection for Predix Timeseries Query

DESCRIPTION:--------THIS PROGRAM generates tkinter list widgets for selecing one or more OSM & Tag for Posting Queries to Predix Timeseries

"""

from tkinter import *
from tkinter import ttk
global frame
global reslist
import pandas as pd
import requests
import json

CertFilePath = r"C:\Certificates\GE_External_Certificate2.pem"

class getOSMs():              
    def __init__(self, UnitPath):
        global lstbox
        main = Tk()
        main.title("OSM & ESN Selection")
        main.geometry("500x500")      
        frame = ttk.Frame(main, padding=(20, 20, 100, 100))
        frame.grid(column=0, row=0, sticky=(N, S, E, W)) 
        unit_pdlist = pd.read_csv(UnitPath)  
        unit_list = unit_pdlist['osm'].astype(str)   +"__"+ unit_pdlist['esn'].astype(str)  
        entries = []
        for osm in unit_list:
            entries.append(osm)
        entrylist = StringVar()
        entrylist.set(entries)    
        lstbox = Listbox(frame, listvariable=entrylist, selectmode=MULTIPLE, width=50, height=20)
        lstbox.grid(column=0, row=0, columnspan=10)        
        def Unitselect():
            global lstbox
            global reslist
            reslist = list()
            Myselection = lstbox.curselection()
            for i in Myselection:
                entireList = lstbox.get(i)
                reslist.append(entireList)            
            for val in reslist:
                print(val) 
        btn = ttk.Button(frame, text="Choose OSMs & EXIT", command=Unitselect)
        btn.grid(column=1, row=1)    
        main.mainloop()




class GetTags():    
    def __init__(self, bearer_token, units):
        global Taglstbox
        main = Tk()
        main.title("Tag Selection")
        main.geometry("500x500")      
        frame = ttk.Frame(main, padding=(20, 20, 100, 100))
        frame.grid(column=0, row=0, sticky=(N, S, E, W)) 
        #Tag_pdlist = pd.read_csv(TagPath)  
        #tag_list = Tag_pdlist['taglist'].astype(str)
        TagList = GetCassandraTags(bearer_token, units)
        
        entrylist = StringVar()
        entrylist.set(TagList)    
        Taglstbox = Listbox(frame, listvariable=entrylist, selectmode=MULTIPLE, width=50, height=20)
        Taglstbox.grid(column=0, row=0, columnspan=10)
        def Tagselect():
            global Taglstbox
            global Tagreslist
            Tagreslist = list()
            Myselection = Taglstbox.curselection()
            for i in Myselection:
                entireList = Taglstbox.get(i)
                Tagreslist.append(entireList)            
            for val in Tagreslist:
                print(val)   
        btn = ttk.Button(frame, text="Choose Tags & EXIT", command=Tagselect)
        btn.grid(column=1, row=1)    
        main.mainloop()



def GetCassandraTags(bearer_token, units):
    url = "https://time-series-ingress.dev.gepowerpredix.com/v1/tags"
    
    headers = {
        'content-type': "application/json",
        'authorization': "Bearer " + bearer_token, 
        'predix-zone-id': "385c05f9-9d4c-43a8-ab3e-7693ec4d50b6",
        'cache-control': "no-cache",
        'postman-token': "fd32d7d6-4d33-ec05-76e9-ceb8a5ce00f7"
        }
    proxies = {
               'http': 'http://iss-americas-pitc-alpharetta.corporate.ge.com:80',
               'https': 'http://iss-americas-pitc-alpharetta.corporate.ge.com:80'
               }        
    print("Please Hold -- Building TagList from Predix Timeseries")
   
    response = requests.request("GET", url, headers=headers, proxies = proxies, verify = CertFilePath)    
    jsonResponse = json.loads(response.text)    
    finaltaglist = []
    
    for esn in units: 
        TagIndex = [i for i, x in enumerate(jsonResponse['results']) if x.split(".")[0] == esn.split('__')[1]]
        for ind in TagIndex:
            tag = jsonResponse['results'][ind]
            finaltaglist.append(tag)
   
    #print(finaltaglist)
    return finaltaglist


def getOSMList(UnitListPath):
    getOSMs(UnitListPath)   
    return reslist

def getTagList(bearer_token, unitlist):
    GetTags(bearer_token, unitlist)   
    return Tagreslist

