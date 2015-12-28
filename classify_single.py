from xml.dom import minidom
from sklearn import metrics
from sklearn.tree import DecisionTreeClassifier
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.multiclass import OneVsRestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn import cross_validation
from sklearn.ensemble import RandomForestClassifier,GradientBoostingClassifier
import numpy as np
# from passage.models import RNN
# from passage.updates import Adadelta
# from passage.layers import Embedding, GatedRecurrent, Dense
from sklearn.preprocessing import MultiLabelBinarizer
import vector_support as vs
from sklearn.svm import NuSVC,SVC,LinearSVC
from sklearn.ensemble import AdaBoostClassifier,GradientBoostingClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.naive_bayes import BernoulliNB,MultinomialNB

import warnings
warnings.filterwarnings("ignore")

wrong_class_log_hander = open("Monitor/wrong_log.txt", 'w+')

fname = "data/2007ChallengeTrainData.xml"
docu = minidom.parse(fname)
docs = docu.getElementsByTagName("doc")

x = []
x2 = []
y = []
x_vector_support = []

path_ndata = "data/ADD_SYNONYMtrue_ADD_PHRASEfalse_ADD_TYPEfalse_ADD_RELATIONtrue.txt"
k = 10
print(path_ndata)
print(k)
print("SVC(LINEAR)")
f_hander = open(path_ndata,"r")
ntexts = f_hander.readlines()

def transfer_multilabel(y_predict, y_text,ml,y_predict_prob):
    y_predict_new = []
    y_text_new = []
    n_record = y_predict.shape[0]
    for idx in range(0, n_record):
        y_predict_record = y_predict[idx]
        y_text_record = y_text[idx]
        y_predict_prob_record = y_predict_prob[idx]
        y_predict_prob_record_idx = np.argmax(y_predict_prob_record)
        y_predict_prob_record_code = ml.classes_[y_predict_prob_record_idx]
        y_text_record_idxs = np.where(1 == y_text_record)[0]
        if y_predict_prob_record_idx in y_text_record_idxs:
            y_predict_new.append(y_predict_prob_record_code)
            y_text_new.append(y_predict_prob_record_code)
        else:
            y_predict_new.append(y_predict_prob_record_code)
            y_text_new.append(ml.classes_[y_text_record_idxs[0]])

    return y_text_new,y_predict_new

idx = 0
for doc in docs:
    codes = doc.getElementsByTagName("code")
    texts = doc.getElementsByTagName("text")

    code_label = []
    for code in codes:
        if code.getAttribute("origin") == "CMC_MAJORITY":
            code_label.append(code.firstChild.data)
    y.append(code_label)

    text_data = ""
    for text in texts:
        text_data = " ".join([text_data, text.firstChild.data])
    x.append(ntexts[idx].decode('utf-8'))
    idx = idx + 1


# tv = TfidfVectorizer(x,stop_words='english',  min_df=0.00002)
tv = CountVectorizer(x, strip_accents='ascii', ngram_range=(1, 1), binary=True)
# tv = HashingVectorizer(x,strip_accents='ascii',ngram_range = (1,1), binary = True)
tfidf_train = tv.fit_transform(x)
#tfidf_train = np.concatenate((tfidf_train.todense(),np.array(x_vector_support)),axis=1)
ml = MultiLabelBinarizer()
y_map = ml.fit_transform(y)
y_map = np.array(y_map)

scores = []
report_y_actual = []
report_y_predict = []

f_scores_all = []
precisions_all = []
recalls_all = []

for loop_stat in range(0, 100):
    f_scores = []
    precisions = []
    recalls = []
    kf = cross_validation.KFold(tfidf_train.shape[0], n_folds=k, shuffle=True)
    for train_index, test_index in kf:
        x_train, x_test = tfidf_train[train_index].toarray(), tfidf_train[test_index].toarray()
        y_train, y_test = y_map[train_index], y_map[test_index]

        model = OneVsRestClassifier(SVC(probability =True,kernel ='linear'))
        model.fit(x_train, y_train)
        y_predict = model.predict(x_test)
        y_predict_prob = model.predict_proba(x_test)
        y_text_new,y_predict_new = transfer_multilabel(y_predict, y_test,ml,y_predict_prob)
        report_y_predict.extend(y_predict_new)
        report_y_actual.extend(y_text_new)

        f_score = metrics.f1_score(report_y_actual, report_y_predict)
        precision = metrics.precision_score(report_y_actual, report_y_predict)
        recall = metrics.recall_score(report_y_actual, report_y_predict)
        f_scores.append(f_score)
        precisions.append(precision)
        recalls.append(recall)

    f_scores_all.append(np.mean(precisions))
    precisions_all.append(np.mean(recalls))
    recalls_all.append(np.mean(f_scores))

m_cls_report = metrics.classification_report(report_y_actual, report_y_predict)
#print(metrics.f1_score(report_y_actual, report_y_predict))
#print(m_cls_report)
print(f_scores_all)
print(precisions_all)
print(recalls_all)
