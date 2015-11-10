from gensim.models import word2vec
from gensim.models import Word2Vec
from xml.dom import minidom
import nltk
from nltk.corpus import stopwords

def process_sentences(path):
    docu = minidom.parse(path)
    docs = docu.getElementsByTagName("doc")
    sentences = []
    for doc in docs:
        codes = doc.getElementsByTagName("code")
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
                sentences.append(words)
    return sentences

action = "test"

if action == "train":
    fname = "data/2007ChallengeTrainData.xml"
    fname2 = "data/2007ChallengeTestDataNoCodes.xml"
    tokenizer = nltk.data.load('tokenizers/punkt/english.pickle')

    sents = []
    sents += process_sentences(fname)
    sents += process_sentences(fname2)

    num_features = 300    # Word vector dimensionality
    min_word_count = 40   # Minimum word count
    num_workers = 4       # Number of threads to run in parallel
    context = 10          # Context window size
    downsampling = 1e-3   # Downsample setting for frequent words
    print("training!")
    # Initialize and train the model (this will take some time)
    model = word2vec.Word2Vec(sents, workers=num_workers, \
                size=num_features, min_count = min_word_count, \
                window = context, sample = downsampling)

    # If you don't plan to train the model any further, calling
    # init_sims will make the model much more memory-efficient.
    model.init_sims(replace=True)

    # It can be helpful to create a meaningful model name and
    # save the model for later use. You can load it later using Word2Vec.load()
    model_name = "model"
    model.save(model_name)
else:
    model = Word2Vec.load("model")
    print(model.most_similar("normal"))



