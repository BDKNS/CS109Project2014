# Libraries

from __future__ import division
import requests
import pandas as pd # pandas
import time
import seaborn as sns
import numpy as np
import re
from csv import writer
from sklearn.cluster import spectral_clustering
from matplotlib.mlab import PCA as mlabPCA
import json
from sklearn.cluster import MeanShift, estimate_bandwidth
from sklearn.datasets.samples_generator import make_blobs
from scipy.cluster.vq import kmeans, vq
import random
import networkx as nx
from matplotlib import pyplot as plt



# Constants

TRAIN_FRACTION = 0.7
NUM_POSTS_IN_QUIZ = 10


user_agent = 'CS109 Data wrangling by BDKNS'
def get_url(url):
    headers = {"User-Agent": user_agent}

    try:
        response = requests.get(url,headers=headers)
        #response.raise_for_status()
    except requests.exceptions.RequestException as e:
        print e
    return response


#Dario TODO
#Does textual analysis comparing training posts to test_post and returns float between 0 (bad) and 1 (good) indicating fit
#May also use other information (user posts, comments, etc.) but they must be before max_date
def textual_analysis(training_posts, max_date, test_post):
    pass
    return 1

#Shawn TODO
#Does category analysis comparing training posts to test_post and returns float between 0 (bad) and 1 (good) indicating fit.
#May also use other information (user posts, comments, etc.) but they must be before max_date
def category_analysis(training_posts, max_date, test_post):
    pass
    return 1


def generate_quiz(max_date, test_post, all_test_posts):    
    viable_post_df = post_df[post_df['published_date'] > max_date]
    viable_ids = list(viable_post_df['id'])
    options = []
    while len(options) < NUM_POSTS_IN_QUIZ - 1:
        option = random.choice(viable_ids)
        if not (option in all_test_posts):
            options.append(option)
    index = random.randint(0,NUM_POSTS_IN_QUIZ-1)
    options.insert(index,test_post)
    return ((options, test_post))


def test_classifier(training_posts, max_date, test_posts):
    successes = 0
    failures = 0
    for test_post in test_posts:
        quiz, correct_answer = generate_quiz(max_date, test_post, test_posts)
        quiz_vals = []
        for option in quiz:
            category_val = category_analysis(training_posts, max_date, test_post)
            textual_val = textual_analysis(training_posts, max_date, test_post)
            
            # right now just sum two mechanisms. We could experiment with weighted average, product, etc.
            total_val = category_val + textual_val
            
            quiz_vals.append((option, total_val))
        quiz_vals.sort(key=lambda tup: tup[1])
        if quiz_vals[-1][0] == correct_answer:
            successes += 1
        else:
            failures += 1
    return successes / (successes + failures)

    
post_df = pd.read_csv('../post.csv')
    
with open('good_users_long.txt') as f:
    good_users = f.readlines()
num_good_users = len(good_users)
num_train_users = int(num_good_users*TRAIN_FRACTION)
good_users_train = good_users[:num_train_users]
good_users_test = good_users[num_train_users:]

test_id = good_users_train[15]

res = get_url('https://www.reddit.com//user/' + str(test_id).rstrip('\n') + '/liked.json')
upvoted_ids = []
if res.status_code == 200:
    response = json.loads(res.content)
    num_upvotes = len(response['data']['children'])
    for i in range(num_upvotes):
         upvoted_ids.append((response['data']['children'][i]['data']['created'],response['data']['children'][i]['data']['id']))

# Note: posts in reverse chronological order, so test is first listed posts; train is later listed posts
cutoff = int(num_upvotes*(1-TRAIN_FRACTION))
sample_max_date = upvoted_ids[cutoff][0]
sample_training_post_tuples = upvoted_ids[cutoff:]
sample_test_post_tuples = upvoted_ids[:cutoff]
sample_training_posts = map(lambda tup: tup[1],sample_training_post_tuples)
sample_test_posts = map(lambda tup: tup[1],sample_test_post_tuples)


print test_classifier(sample_training_posts, sample_max_date, sample_test_posts)













