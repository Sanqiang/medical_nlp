import numpy as np
from sklearn.neighbors import NearestNeighbors,RadiusNeighborsClassifier
import linecache
from scipy.stats import pearsonr

word2idx = {}
path_raw_vector = "C:\glove\glove_6B\glove.6B.300d.txt"
dims = 300

def init():
    raw_vector_handler = open(path_raw_vector,"r", encoding='utf-8')
    idx = 1
    for line in raw_vector_handler:
        items = line.split()
        word = items[0]
        # vector = np.array(items[1:]).astype(np.float)
        word2idx[word] = idx
        idx = idx + 1

def get_word_vector(word):
    idx = word2idx[word]
    line = linecache.getline(path_raw_vector, idx)
    items = line.split()
    vector = np.array(items[1:]).astype(np.float)
    return vector

def get_sim_vector(vector1, vector2):
    pair = pearsonr(vector1,vector2)
    return pair[0]

def get_sent_vector(sent):
    sent = sent.lower()
    vector_sum = np.zeros(dims)
    words = sent.split()
    count = 0
    for word in words:
        if word in word2idx:
            vector = get_word_vector(word)
            vector_sum = [vector_sum[idx]+vector[idx] for idx in range(0,dims)]
            # vector_sum = map(sum, zip(vector_sum,vector))
            count = count + 1
    if count > 0:
        vector_sum = [ele/float(count) for ele in vector_sum]
    return vector_sum