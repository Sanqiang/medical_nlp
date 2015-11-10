from xml.dom import minidom
import nltk
from nltk.corpus import stopwords

tokenizer = nltk.data.load('tokenizers/punkt/english.pickle')
path = "data/2007ChallengeTrainData.xml"
docu = minidom.parse(path)
docs = docu.getElementsByTagName("doc")
for doc in docs:
    texts = doc.getElementsByTagName("text")
    text_data = ""
    for text in texts:
        text_data = " ".join([text_data, text.firstChild.data])

    raw_sentences = tokenizer.tokenize(text_data.strip())
    for raw_sentence in raw_sentences:
        if len(raw_sentence) > 0:
            words = raw_sentence.lower().split()
            stops = set(stopwords.words("english"))
            words = [w for w in words if not w in stops]
