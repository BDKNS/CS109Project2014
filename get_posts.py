import requests
import json
import time
import re
import os
import sys
from csv import writer


#given a url as a string, it will try to get it
def get_url(url):
    headers = {"User-Agent": user_agent}

    try:
        response = requests.get(url,headers=headers)
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        print e
    return response


#
user_agent = 'CS109 Data wrangling by BDKNS'
#Delay is set to 2 as we are using the unauthenticated reddit API
#and we can only make a call every 2 seconds
delay = 2.0
#total number of iterations to run
#each iteration grabs 100 posts, and takes at least
#2 seconds to run
total = 10

#post_id, post_title, subreddit_title, subreddit_id
#self_post, num_comments, score, textual content
#gilded, publication date, edited, mature, moderated
out_file = open('lotsofposts.csv', 'w')
csv = writer(out_file)
csv.writerow(['id','title','subreddit_title','subreddit_id','self_post',
       'num_comments','score','text_content','gilded','published_date', 'mature','user_id','url','permalink'])

#This is used to strip out none alpha-numeric characters
pattern = re.compile('[^0-9a-zA-Z\.]+', re.UNICODE)

#last_id.txt should have the last ID we grabed from reddit
#if there is a value, we grab values after it, if there is
#nothing we assume we need to start from the beggining

#this creates the file if it is missing
if not os.path.exists('last_id.txt'):
    file('last_id.txt', 'w').close()
f = open('last_id.txt', 'r')
after = f.readline().rstrip('\n')
f.close()

#If there is an error getting content from reddit, we just wait 5 seconds
#and try again
keep_trying = True
error_count = 0

#A count of how many iterations we have been through
i = 0
done = False

start_time = time.time()

while not done:
    query_time = time.time()

    while keep_trying == True:
        try:
            if after == None:
                url = "http://reddit.com/r/all/.json?limit=100"
                response = get_url(url)
                response = json.loads(response.content)
                after = response['data']['after']
                keep_trying = False

            else:
                url = "http://reddit.com/r/all/.json?limit=100&after="+after
                response = get_url(url)
                response = json.loads(response.content)
                after = response['data']['after']
                keep_trying = False

        except Exception as e:
            print e
            if (error_count < 10):
                error_count = error_count + 1
                print "error occured, sleeping for 5 seconds"
                time.sleep(5)
                #authenticate again
                pass
            else:
                keep_trying = False

    keep_trying = True
    f = open('last_id.txt', 'w')
    f.write(after)
    f.close()

    for post in response['data']['children']:
        #post = dict(post)

        values = [post['data']['name'], post['data']['title'],post['data']['subreddit'],
            post['data']['subreddit_id'], post['data']['is_self'], post['data']['num_comments'],
            post['data']['score'], post['data']['selftext'].replace(","," "), post['data']['gilded'],
            post['data']['created_utc'], post['data']['over_18'], post['data']['author'], post['data']['url'],
            post['data']['permalink']]
        #make sure the encoding of all text is utf8
        values = [(value.encode('utf8') if hasattr(value, 'encode') else value)
                  for value in values]
        #strip out any non alpha numeric chars
        #to avoid problems later when we try to read in the CSV
        values = [(pattern.sub(' ', s) if type(s) == type(str) else s)
                  for s in values]
        csv.writerow(values)

    i = i + 1

    if i >= total:
        done = True

    #we need to wait 2 seconds between API calls
    run_time = delay - (time.time() - query_time)
    if delay - run_time >= 0:
        time.sleep((delay - run_time))
    #time.sleep(delay - 0.5)
    if ((i * 100) % 500) == 0:
        print "Total posts: ", i * 100
        print "Average time between API calls: ",(time.time() - start_time)/i

out_file.close()
