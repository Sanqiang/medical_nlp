from xml.dom import minidom
from sklearn import metrics
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.multiclass import OneVsRestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn import cross_validation
import numpy as np
from passage.models import RNN
from passage.updates import Adadelta
from passage.layers import Embedding, GatedRecurrent, Dense
from sklearn.preprocessing import MultiLabelBinarizer

fname = "data/2007ChallengeTrainData.xml"
docu = minidom.parse(fname)
docs = docu.getElementsByTagName("doc")

x = []
y = []


def transfer_multilabel(y_predict, y_text):
    y_predict_new = []
    y_text_new = []
    n_record = y_predict.shape[0]
    for idx in range(0, n_record):
        y_predict_record = y_predict[idx]
        y_text_record = y_text[idx]
        find_items = np.where(True == np.logical_and(y_predict_record, y_text_record))
        if len(find_items[0]) > 0:
            y_predict_new.append(find_items[0][0])
            y_text_new.append(find_items[0][0])
        else :
            y_predict_new.append(0)
            y_text_new.append(np.where(1 == y_text_record)[0][0])
    return y_predict_new, y_text_new


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
    x.append(text_data)




# tv = TfidfVectorizer(x,stop_words='english',  min_df=0.00002)
tv = CountVectorizer(x, strip_accents='ascii', ngram_range=(1, 2), binary=True)
# tv = HashingVectorizer(x,strip_accents='ascii',ngram_range = (1,1), binary = True)
tfidf_train = tv.fit_transform(x)
y = MultiLabelBinarizer().fit_transform(y)
y = np.array(y)
method = "trad"

scores = []
report_y_actual = []
report_y_predict = []

kf = cross_validation.KFold(tfidf_train.shape[0], n_folds=tfidf_train.shape[0], shuffle=True)
for train_index, test_index in kf:
    x_train, x_test = tfidf_train[train_index].toarray(), tfidf_train[test_index].toarray()
    y_train, y_test = y[train_index], y[test_index]
    if method == "trad":
        model = OneVsRestClassifier(LogisticRegression(C=978))
        model.fit(x_train, y_train)
        y_predict = model.predict(x_test)
        y_predict_new, y_text_new = transfer_multilabel(y_predict, y_test)
        report_y_predict.extend(y_predict_new)
        report_y_actual.extend(y_text_new)
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
m_cls_report = metrics.classification_report(report_y_actual, report_y_predict)
print(m_cls_report)