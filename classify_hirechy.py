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
from sklearn.feature_selection import SelectFromModel
from sklearn.kernel_ridge import KernelRidge
from sklearn.neighbors import KDTree

import warnings
warnings.filterwarnings("ignore")

code2cate = {}
cate2code = {}
defaultcode = {}
defaultcode["0"] = "277.00"

f_hander = open("structure.txt","r")
structures = f_hander.readlines()
for structure in structures:
    pair = structure.split(" ")
    code = pair[0]
    cate = pair[1]
    code2cate[code] = cate
    cate2code[cate] = code
    if len(pair) == 3:
        defaultcode[cate] = code

wrong_class_log_hander = open("Monitor/wrong_log_hirerachy.txt", 'w+')

fname = "data/2007ChallengeTrainData.xml"
docu = minidom.parse(fname)
docs = docu.getElementsByTagName("doc")

x = []
x2 = []
y = []
y_cate = []
x_vector_support = []

path_ndata = "java_support\medical_nlp\ADD_SYNONYMfalse_ADD_PHRASEtrue_ADD_TYPEfalse_ADD_RELATIONtrue.txt"
print(path_ndata)
f_hander = open(path_ndata,"r")
ntexts = f_hander.readlines()

def transfer_multilabel(y_predict_code_map,y_test_code_cur_map,ml,y_predict_code_map_prob,cate_cur):
    y_predict_new = []
    y_text_new = []
    for ind in range(0,len(y_predict_code_map)):
        y_predict_code_map_cur = y_predict_code_map[ind]
        if 1 in y_predict_code_map_cur:
            y_predict_code_cur = ml.classes_[np.where(y_predict_code_map_cur == 1)[0][0]]
        else:
            y_predict_code_cur = defaultcode[cate_cur]
        y_predict_new.append(y_predict_code_cur)

        y_test_code_cur_map_cur = y_test_code_cur_map[ind]
        if 1 in y_test_code_cur_map_cur:
            y_test_code_cur_cur =  ml.classes_[np.where(y_test_code_cur_map_cur == 1)[0][0]]
        else:
            y_test_code_cur_cur = "error_in_first_layer"
        y_text_new.append(y_test_code_cur_cur)

        if y_predict_code_cur != y_test_code_cur_cur:
            x= 1

    return y_text_new,y_predict_new

def detailed_train(ml, y_predict_record_prob):
    code = ml.classes_[np.argmax(y_predict_record_prob)]
    return code

idx = 0
for doc in docs:
    codes = doc.getElementsByTagName("code")
    texts = doc.getElementsByTagName("text")

    code_label = []
    code_label_cate = []
    for code in codes:
        if code.getAttribute("origin") == "CMC_MAJORITY":
            code_label.append(code.firstChild.data)
            code_label_cate.append(code2cate[code.firstChild.data])
    y_cate.append(code_label_cate)
    y.append(code_label)

    text_data = ""
    for text in texts:
        text_data = " ".join([text_data, text.firstChild.data])
    x.append(ntexts[idx].decode('utf-8'))
    # x.append(text_data)
    idx = idx + 1


# tv = TfidfVectorizer(x,stop_words='english',  min_df=0.00002)
tv = CountVectorizer(x, strip_accents='ascii', ngram_range=(1, 1), binary=True)
# tv = HashingVectorizer(x,strip_accents='ascii',ngram_range = (1,1), binary = True)
tfidf_train = tv.fit_transform(x)
#tfidf_train = np.concatenate((tfidf_train.todense(),np.array(x_vector_support)),axis=1)
ml = MultiLabelBinarizer()
y_map = ml.fit_transform(y)
y_map = np.array(y_map)
ml_cate = MultiLabelBinarizer()
y_map_cate = ml_cate.fit_transform(y_cate)
y_map_cate = np.array(y_map_cate)


f_scores = []
for loop_stat in range(0,1):
    scores = []
    report_y_actual = []
    report_y_predict = []
    kf = cross_validation.KFold(tfidf_train.shape[0], n_folds=5, shuffle=True)
    loop = 0
    for train_index, test_index in kf:
        x_train, x_test = tfidf_train[train_index].toarray(), tfidf_train[test_index].toarray()
        y_train_cate_map, y_test_cate_map = y_map_cate[train_index], y_map_cate[test_index]
        y_train_code_map,y_test_code_map = y_map[train_index], y_map[test_index]
        y_train_code, y_test_code = np.array(ml.inverse_transform(y_train_code_map)),np.array(ml.inverse_transform(y_test_code_map))
        y_train_cate,y_test_cate = np.array(ml_cate.inverse_transform(y_train_cate_map)),np.array(ml_cate.inverse_transform(y_test_cate_map))
        # classify the category
        model_cate = OneVsRestClassifier(LogisticRegression())
        model_cate.fit(x_train, y_train_cate_map)
        y_predict_cate_map = model_cate.predict(x_test)
        y_predict_cate = np.array(ml_cate.inverse_transform(y_predict_cate_map))
        y_predict_cate_unique = reduce(lambda a,b:set(a)|set(b)  ,y_predict_cate)
        for cate_cur in y_predict_cate_unique:
            if cate_cur not in defaultcode:
                y_text_new,y_predict_new = transfer_multilabel(y_predict_cate_map,y_test_cate_map,ml_cate,None,"0")
                report_y_predict.extend(y_predict_new)
                report_y_actual.extend(y_text_new)
            else:
                continue
                idx_test_cur = [ind for ind in range(0,len(y_predict_cate)) if cate_cur in y_predict_cate[ind]]
                idx_train_cur = [ind for ind in range(0,len(y_train_cate)) if cate_cur in y_train_cate[ind]]
                x_train_cur = x_train[idx_train_cur]
                x_test_cur = x_test[idx_test_cur]
                y_train_code_cur = []
                y_test_code_cur = []
                for category_predict_tuple in y_test_code[idx_test_cur]:
                    codes = []
                    if len(category_predict_tuple) == 0:
                        codes.append(defaultcode[cate_cur])
                    else:
                        codes.extend([v for v in category_predict_tuple if v.startswith(cate_cur)])
                    y_test_code_cur.append(codes)
                y_test_code_cur_map = ml.transform(y_test_code_cur)
                for y_train_code_tuple in y_train_code[idx_train_cur]:
                    codes = []
                    if len(y_train_code_tuple) == 0:
                        codes.append(defaultcode[cate_cur])
                    else:
                        codes.extend([v for v in y_train_code_tuple if v.startswith(cate_cur)])
                    y_train_code_cur.append(codes)
                y_train_code_cur_map = ml.transform(y_train_code_cur)

                model_code = DecisionTreeClassifier()
                model_code.fit(x_train_cur,y_train_code_cur_map)
                y_predict_code_map = model_code.predict(x_test_cur)
                y_predict_code_map_prob = model_code.predict_proba(x_test_cur)
                y_text_new,y_predict_new = transfer_multilabel(y_predict_code_map,y_test_code_cur_map,ml,y_predict_code_map_prob,cate_cur)
                report_y_predict.extend(y_predict_new)
                report_y_actual.extend(y_text_new)

    loop = loop + 1
        # if loop % 10 == 0:
        #     print(metrics.f1_score(report_y_actual, report_y_predict))

    m_cls_report = metrics.classification_report(report_y_actual, report_y_predict)
    print(m_cls_report)
    f_score = metrics.f1_score(report_y_actual, report_y_predict)
    print(f_score)
    f_scores.append(f_score)
    break
    #print(m_cls_report)

print(f_scores)
