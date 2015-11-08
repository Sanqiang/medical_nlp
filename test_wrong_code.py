from xml.dom import minidom
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.preprocessing import MultiLabelBinarizer
import numpy as np
from sklearn.multiclass import OneVsRestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn import metrics

wrong_class_log_hander = open("Monitor/wrong_log.txt", 'w+')
fname = "data/2007ChallengeTrainData.xml"
docu = minidom.parse(fname)
docs = docu.getElementsByTagName("doc")

x = []
y = []

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

tv = CountVectorizer(x, strip_accents='ascii', ngram_range=(1, 1), binary=True)
tfidf_train = tv.fit_transform(x)
ml = MultiLabelBinarizer()
y_map = ml.fit_transform(y)
y_map = np.array(y_map)
model = OneVsRestClassifier(LogisticRegression())
model.fit(tfidf_train, y_map)
predict_y = model.predict(tfidf_train)

y_predict_new = []
y_text_new = []

n_record = predict_y.shape[0]
for idx in range(0, n_record):
    predict_y_item = predict_y[idx]
    true_y_item = y_map[idx]
    find_items = np.where(True == np.logical_and(predict_y_item, true_y_item))
    if len(find_items[0]) == 0:
        if len(np.where(1 == predict_y_item)[0]) > 0:
            code_wrong = ml.classes_[np.where(1 == predict_y_item)[0][0]]
        else:
            code_wrong = "error"
        code = ml.classes_[np.where(1 == true_y_item)[0][0]]
        y_predict_new.append(code_wrong)
        y_text_new.append(code)
        wrong_class_log_hander.write(str(idx+1))
        wrong_class_log_hander.write("\t")
        wrong_class_log_hander.write(code)
        wrong_class_log_hander.write("\t")
        wrong_class_log_hander.write(code_wrong)
        wrong_class_log_hander.write("\n")
    elif len(find_items[0]) > 0:
        code = ml.classes_[find_items[0][0]]
        y_predict_new.append(code)
        y_text_new.append(code)

m_cls_report = metrics.classification_report( y_text_new, y_predict_new)
print(m_cls_report)

