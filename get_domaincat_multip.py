#This is so / will cast ints to floats
from __future__ import division

import pandas as pd # pandas
import time
import numpy as np
import re
from csv import writer
import requests
from multiprocessing import Pool

#Contains domains and categories
#used to store results of previous domain category lookups
#reduces the number of network calls we have to make
domain_categories_csv = 'domain_categories.csv'

#Posts we are using for our analysis and modeling
#posts_csv = '100PostPerSubreddit.csv'
#posts_csv = 'userliked_pipedelimited.csv'
posts_csv = 'user_liked.csv'
delimeter = '|'

#given a url http://cnn.com/worldnews/storyoftheday
#this method will strip out the http:// and /storyoftheday
#resulting in cnn.com
def get_domain(url):
    #strip http:// out
    url = re.sub('http\:\/\/', '', url)
    #in the initial run, I forgot to include https
    url = re.sub('https\:\/\/', '', url)

    #strip out trailing URI details so only domain is left
    return re.sub('/.*', '', url)


#attempts to retrive content at the supplied URL
def get_url(url):
    response = None
    try:
        response = requests.get(url)
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        print e
    return response

def get_cat(domain):

    #build the URL to check the category
    url = "https://domain.opendns.com/" + domain
    response = get_url(url)
    #This ugly regex is used to get the word Tagged: and the tags on the same line from
    #the html
    response = re.sub('re.sub("<.*?>", " ", html)', '', response.content.rstrip('\n'))
    response = re.sub('Tag.*\:\n', 'Tagged:', response)
    response = re.sub('\<span class=\"normal\"\>\n', '', response)

    #Find the line that contains the domain category information
    reg_exp = re.compile("Tagged:(.*)")
    category = reg_exp.findall(response)

    if len(category) > 0:
        #onyl really care about the first response, should
        #look into refactoring to use a different method
        category = category[0]
        category = re.sub('\t+', '', category)
        category = re.sub('\</span\>', '', category)
        category = category.strip()
        category = category.replace(","," ")
        return {'category': category, 'domain': domain}
    else:
      return {}


if __name__ == '__main__':


  postdf=pd.read_csv(posts_csv,sep=delimeter)
  postdf.dropna(inplace=True)
  postdf['domain'] = postdf['url'].map(get_domain)
  print "Total domains: ",len(postdf['domain'].unique())

  domain_cat=pd.read_csv(domain_categories_csv)
  domains_we_have = domain_cat['domain'].values
  domains_from_posts = postdf['domain'].unique()
  domains_to_lookup = list(set(domains_from_posts) - set(domains_we_have))

  print "Total domains to lookup: ",len(domains_to_lookup)

  df = pd.DataFrame()

  start_time = time.time()
  p = Pool(8)
  result = p.map(get_cat,domains_to_lookup)
  p.close()
  p.join()
  print "Finished in ",time.time() - start_time," seconds"

  df = df.from_dict(result)
  domain_cat = domain_cat.append(df)
  domain_cat.to_csv(domain_categories_csv, sep=delimeter, encoding='utf-8',index=False)
