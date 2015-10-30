from xml.dom import minidom
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn import cross_validation
import numpy as np
from sklearn import metrics

fname = "data/2007ChallengeTrainData.xml"
docu = minidom.parse(fname)
docs = docu.getElementsByTagName("doc")

x=[]
y=[]

for doc in docs:
    codes = doc.getElementsByTagName("code")
    texts = doc.getElementsByTagName("text")
    for code in codes:
        if code.getAttribute("origin") == "CMC_MAJORITY":

            code_label = code.firstChild.data
            text_data  = ""
            for text in texts:
                text_data = " ".join([text_data, text.firstChild.data])

            x.append(text_data)
            y.append(code_label)


tv = TfidfVectorizer( stop_words='english',  min_df=0.00002)
tfidf_train= tv.fit_transform(x)
y = np.array(y)

kf = cross_validation.KFold(tfidf_train.shape[0], n_folds=3)
for train_index, test_index in kf:
    x_train, x_test = tfidf_train[train_index], tfidf_train[test_index]
    y_train, y_test = y[train_index], y[test_index]
    model = LogisticRegression()
    model.fit(x_train, y_train)
    score = model.score(x_test,y_test)
    print(score)