from xml.dom import minidom


wrong_class_log_hander = open("Monitor/wrong_log.txt", 'w+')
data_hander = open("data/data.txt", 'w+')

fname = "data/2007ChallengeTrainData.xml"
docu = minidom.parse(fname)
docs = docu.getElementsByTagName("doc")

x = []
y = []

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
    x.append(text_data)

    if True:
        idx = idx + 1
        data_hander.write(str(idx))
        data_hander.write("\t")
        data_hander.write(str(code_label))
        data_hander.write("\t")
        data_hander.write(text_data)
        data_hander.write("\n")
