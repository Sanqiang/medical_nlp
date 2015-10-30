from xml.dom import minidom
from sklearn.feature_extraction.text import CountVectorizer,HashingVectorizer,TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier,AdaBoostClassifier
from sklearn import cross_validation
import numpy as np
from sklearn import metrics
from sklearn.svm import SVC
from passage.models import RNN
from passage.updates import Adadelta
from passage.layers import Embedding, GatedRecurrent, LstmRecurrent, Dense
from passage.preprocessing import Tokenizer
from sklearn.metrics import roc_auc_score
from sklearn.preprocessing import MultiLabelBinarizer

fname = "data/2007ChallengeTrainData.xml"
docu = minidom.parse(fname)
docs = docu.getElementsByTagName("doc")

x=[]
y=[]

for doc in docs:
    codes = doc.getElementsByTagName("code")
    texts = doc.getElementsByTagName("text")
    if codes[0].getAttribute("origin") == "CMC_MAJORITY" and codes[1].getAttribute("origin") != "CMC_MAJORITY":
        code_label = codes[0].firstChild.data
        #if code_label == "786.2" or code_label == "599.0" or code_label == "593.70" or code_label == "780.6":
        text_data  = ""
        for text in texts:
            text_data = " ".join([text_data, text.firstChild.data])
        x.append(text_data)
        y.append(code_label)
        #print(text_data)
        #print(code_label)


#tv = TfidfVectorizer(x,stop_words='english',  min_df=0.00002)
tv = CountVectorizer(x,strip_accents='ascii',ngram_range = (1,2), binary = True)
#tv = HashingVectorizer(x,strip_accents='ascii',ngram_range = (1,1), binary = True)
tfidf_train= tv.fit_transform(x)
y = np.array(y)
method = "trad"

scores = []
report_y_actual = []
report_y_predict = []

kf = cross_validation.KFold(tfidf_train.shape[0], n_folds=10,shuffle=True)
for train_index, test_index in kf:
    x_train, x_test = tfidf_train[train_index].toarray(),tfidf_train[test_index].toarray()
    y_train, y_test = y[train_index], y[test_index]
    if method == "trad":
        model = LogisticRegression(C=100)
        model.fit(x_train, y_train)
        predicty = model.predict(x_test)

        #m_precision = metrics.precision_score(y_test,predicty)
        #m_recall = metrics.recall_score(y_test,predicty)
        #m_cls_report = metrics.classification_report(y_test, predicty)
        report_y_predict.extend(predicty)
        report_y_actual.extend(y_test)
        #print(m_precision)
        #print(m_recall)
        #print(m_cls_report)
        #scores.append((m_precision, m_recall))
    elif method == "rnn":
        layers = [
            Embedding(size=256, n_features=tfidf_train.shape[1]),
            GatedRecurrent(size=512, activation='tanh', gate_activation='steeper_sigmoid',
                           init='orthogonal', seq_output=False, p_drop=0.75),
            Dense(size=1, activation='sigmoid', init='orthogonal')
        ]
        model = RNN(layers=layers, cost='bce', updater=Adadelta(lr=0.5))
        model.fit(x_train, y_train, n_epochs=10)
        pr_teX = model.predict(x_test).flatten()
        predY = np.ones(len(y_test))
        predY[pr_teX < 0.5] = -1
        print(score)
        scores.append(score)
#print("Result:")
#print(np.mean(scores))
m_cls_report = metrics.classification_report(report_y_actual, report_y_predict)
print(m_cls_report)