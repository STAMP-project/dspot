import numpy as np
import collections
import pickle
import argparse
import json
from google.colab import drive

from keras import Input
from keras.engine import Model
from keras.utils import np_utils
from keras.layers.core import Dense, RepeatVector
from keras.layers.recurrent import LSTM
from keras.models import load_model
from keras.layers.wrappers import TimeDistributed


class Config:
    def __init__(self):
        self.UNKNOWN_TOKEN = "**UNK**"
        self.PAD_TOKEN = "**PAD**"
        self.INPUT_VOCAB_SIZE = 4096
        self.OUTPUT_VOCAB_SIZE = 60000
        self.N_NEIGHBORS = 10
        self.KTH_COMMON = 1

        self.SEQ_LEN = 5
        self.HIDDEN_LAYER_SIZE = 80
        self.HIDDEN_LAYER_SIZE2 = 350  # 3500
        self.BATCH_SIZE = 32
        self.NUM_EPOCHS = 1  # 5
        self.ACCURACY = 0.8  # 0.995
        self.ACCURACY2 = 0.9
        self.PLATEAU_LEN = 1
        self.CHUNK_SIZE1 = 2500  # 25000
        self.CHUNK_SIZE2 = 2000  # 20000

        self.PROCESSED_FILE = "vocab.pkl"
        self.TRAINING_FILE = "training.csv"  # space separated
        self.VALIDATION_FILE = "validation.csv"  # space separated
        self.MODEL_FILE = "model.h5"
        self.CONFIG_FILE = "config.json"


def easy_path(file_path):
    return '/content/gdrive/My Drive/C2N/model/' + file_path


def easy_open(file_path, mode):
    return open(easy_path(file_path), mode)


def parse_args():
    parser = argparse.ArgumentParser()

    parser.add_argument('-p', action='store_true', default=False,
                        dest='is_pload',
                        help='Load processed training and validation data.  Sets -i and -o options.')
    parser.add_argument('-i', action='store_true', default=False,
                        dest='is_iload',
                        help='Load input vocabulary')
    parser.add_argument('-o', action='store_true', default=False,
                        dest='is_oload',
                        help='Load output vocabulary')
    parser.add_argument('-a', action='store_true', default=False,
                        dest='load_model1',
                        help='Load encoder model from file')
    parser.add_argument('-b', action='store_true', default=False,
                        dest='load_model2',
                        help='Load LSTM model from file')

    return parser.parse_args()


def apply_fun(arr, fun):
    if not isinstance(arr, list):
        fun(arr)
    else:
        for x in arr:
            apply_fun(x, fun)


def map_fun(arr, fun):
    for i in range(len(arr)):
        if not isinstance(arr[i], list):
            arr[i] = fun(arr[i])
        else:
            map_fun(arr[i], fun)


def load_inputs(input_file, map=None):
    input_arr = []
    output_arr = []
    prefix2 = "1ID:-1:"
    prefix3 = "1ID:0:"
    header_len = 2
    lines = []
    with easy_open(input_file, "r") as file:
        for line in file:
            line = line.rstrip()
            lines.append(line)
    j = 0
    for line in lines:
        tokens = line.split()
        if not tokens[1].startswith(prefix2):
            output = tokens[1].split(":")[2]
            if map is None or output in map[1]:
                output_arr.append(output)
                j += 1
                x = []
                input_arr.append(x)
                length = min(len(tokens) - header_len, config.SEQ_LEN * config.N_NEIGHBORS)
                for i in range(length):
                    token = tokens[i + header_len]
                    if token.startswith(prefix2):
                        token = token[len(prefix2):]
                    elif token.startswith(prefix3):
                        token = token[len(prefix3):]
                    elif token.startswith("1ID:"):
                        token = "1ID"
                    x.append(token)
                for i in range(length, config.SEQ_LEN * config.N_NEIGHBORS):
                    x.append(config.PAD_TOKEN)
                x.reverse()
    print("Read {} records from {}".format(len(input_arr), input_file))
    return (input_arr, output_arr)


def get_word2index(vocab_size, token_freqs, kth=1):
    if vocab_size == None:
        vocab_size = len(token_freqs)
    common_words = token_freqs.most_common(vocab_size * kth)
    if (kth - 1) * vocab_size > len(common_words):
        del common_words[:]
    else:
        del common_words[0:(kth - 1) * vocab_size]
    vocab_size = len(common_words) + 2
    print("vocab_size {}".format(vocab_size))
    word2index = {x[0]: i + 2 for i, x in enumerate(common_words)}
    word2index[config.PAD_TOKEN] = 0
    word2index[config.UNKNOWN_TOKEN] = 1
    index2word = {v: k for k, v in word2index.items()}
    print("Done with word to index".format(vocab_size))
    return (vocab_size, word2index, index2word)


def get_index_map(arr, vocab_size=None, kth=1):
    token_freqs = collections.Counter()

    def count(x):
        token_freqs[x] += 1

    apply_fun(arr, count)
    return get_word2index(vocab_size, token_freqs, kth)


def indexify_array(arr, map):
    def f(x):
        if x in map[1]:
            return map[1][x]
        else:
            return map[1][config.UNKNOWN_TOKEN]

    map_fun(arr, f)
    ret = np.array(arr)
    print("Indexified array with shape {}".format(ret.shape))
    return ret


def load_and_process_arrays():
    if results.is_pload:
        results.is_iload = True
        results.is_oload = True
        (training_arr, validation_arr) = pickle.load(easy_open(
            "p_" + str(config.INPUT_VOCAB_SIZE) + "_" + str(config.OUTPUT_VOCAB_SIZE) + "_" + config.PROCESSED_FILE,
            "rb"))
    else:
        training_arr = load_inputs(config.TRAINING_FILE)
        validation_arr = load_inputs(config.VALIDATION_FILE)
    if not results.is_iload and not results.is_oload:
        i_map = get_index_map(training_arr[0], config.INPUT_VOCAB_SIZE)
        o_map = get_index_map(training_arr[1], config.OUTPUT_VOCAB_SIZE, config.KTH_COMMON)
    elif not results.is_oload and results.is_iload:
        i_map = pickle.load(easy_open("i_" + str(config.INPUT_VOCAB_SIZE) + "_" + config.PROCESSED_FILE, 'rb'))
        o_map = get_index_map(training_arr[1], config.OUTPUT_VOCAB_SIZE, config.KTH_COMMON)
    elif not results.is_iload and results.is_oload:
        o_map = pickle.load(easy_open("o_" + str(config.OUTPUT_VOCAB_SIZE) + "_" + config.PROCESSED_FILE, 'rb'))
        i_map = get_index_map(training_arr[0], config.INPUT_VOCAB_SIZE)
    else:
        i_map = pickle.load(easy_open("i_" + str(config.INPUT_VOCAB_SIZE) + "_" + config.PROCESSED_FILE, 'rb'))
        o_map = pickle.load(easy_open("o_" + str(config.OUTPUT_VOCAB_SIZE) + "_" + config.PROCESSED_FILE, 'rb'))
    if not results.is_iload:
        pickle.dump(i_map, easy_open("i_" + str(config.INPUT_VOCAB_SIZE) + "_" + config.PROCESSED_FILE, 'wb'))
    if not results.is_oload:
        pickle.dump(o_map, easy_open("o_" + str(config.OUTPUT_VOCAB_SIZE) + "_" + config.PROCESSED_FILE, 'wb'))
    if not results.is_pload:
        training_arr = None  # make sure that GC garbage collects the array
        training_arr = load_inputs(config.TRAINING_FILE, o_map)
        training_arr = (indexify_array(training_arr[0], i_map), indexify_array(training_arr[1], o_map))
        validation_arr = (indexify_array(validation_arr[0], i_map), indexify_array(validation_arr[1], o_map))
        pickle.dump((training_arr, validation_arr), easy_open(
            "p_" + str(config.INPUT_VOCAB_SIZE) + "_" + str(config.OUTPUT_VOCAB_SIZE) + "_" + config.PROCESSED_FILE,
            "wb"))
    return (training_arr, validation_arr, i_map, o_map)


# autoencoder

def generate_sequence_for_encoder(arr, input_vocab_size):
    c = 0
    arr = arr.reshape([-1, config.N_NEIGHBORS])
    indices = list(range(len(arr)))
    np.random.shuffle(indices)
    arr = arr[indices, :]
    while True:
        if (c + config.CHUNK_SIZE1 > len(arr)):
            np.random.shuffle(indices)
            arr = arr[indices, :]
            c = 0
        print("Generating data starting at index {} of length {}".format(c, config.CHUNK_SIZE1))
        arr_np = np_utils.to_categorical(arr[c:c + config.CHUNK_SIZE1, :].reshape([-1]),
                                         num_classes=input_vocab_size).reshape(
            [-1, config.N_NEIGHBORS, input_vocab_size])
        c = c + config.CHUNK_SIZE1
        yield arr_np


def get_generators_for_encoder(training_arr, validation_arr, x_vocab_size):
    training_generator = generate_sequence_for_encoder(training_arr[0], x_vocab_size)
    eval_generator = generate_sequence_for_encoder(validation_arr[0], x_vocab_size)
    return (training_generator, eval_generator)


def create_autoencoder(input_vocab_size):
    inputs = Input(shape=(config.N_NEIGHBORS, input_vocab_size))
    encoded = LSTM(config.HIDDEN_LAYER_SIZE)(inputs)

    decoded = RepeatVector(config.N_NEIGHBORS)(encoded)
    decoded = LSTM(input_vocab_size, return_sequences=True)(decoded)
    decoded = TimeDistributed(Dense(input_vocab_size, activation='softmax'))(decoded)

    autoencoder = Model(inputs, decoded)
    encoder = Model(inputs, encoded)
    encoder.compile(loss="categorical_crossentropy", optimizer="adam", metrics=["accuracy"])
    autoencoder.compile(loss="categorical_crossentropy", optimizer="adam", metrics=["accuracy"])
    autoencoder.summary()
    encoder.summary()
    return autoencoder, encoder


def train_encoder_aux(autoencoder, encoder, training_generator, eval_generator, n_chunks):
    print("Starting encoder training with number of chunks = {} ...".format(n_chunks))
    counter = 0
    for j in range(n_chunks):
        train_data = next(training_generator)
        eval_data = next(eval_generator)
        print("Running epoch 1 on data[{}] with train data shape being {}\n".format(j + 1, train_data.shape))
        print("Running epoch 1 on data[{}] with validation data shape being {}\n".format(j + 1, eval_data.shape))
        score, acc = autoencoder.evaluate(eval_data, eval_data, batch_size=config.BATCH_SIZE)
        print("Test score: %.3f, accuracy: %.3f" % (score, acc))
        if acc > config.ACCURACY:
            counter += 1
            if counter >= config.PLATEAU_LEN:
                autoencoder.save(easy_path("autoencoder." + str(config.INPUT_VOCAB_SIZE) + "_" + str(
                    config.HIDDEN_LAYER_SIZE) + "." + config.MODEL_FILE))
                encoder.save(easy_path("encoder." + str(config.INPUT_VOCAB_SIZE) + "_" + str(
                    config.HIDDEN_LAYER_SIZE) + "." + config.MODEL_FILE))
                return
        else:
            counter = 0
        autoencoder.fit(train_data, train_data, batch_size=config.BATCH_SIZE, epochs=1,
                        validation_data=(eval_data, eval_data))
        if j % 10 == 0:
            autoencoder.save(easy_path("autoencoder." + str(config.INPUT_VOCAB_SIZE) + "_" + str(
                config.HIDDEN_LAYER_SIZE) + "." + config.MODEL_FILE))
            encoder.save(easy_path("encoder." + str(config.INPUT_VOCAB_SIZE) + "_" + str(
                config.HIDDEN_LAYER_SIZE) + "." + config.MODEL_FILE))


def train_encoder(training_arr, validation_arr, input_vocab_size):
    training_generator, eval_generator = get_generators_for_encoder(training_arr, validation_arr, input_vocab_size)
    autoencoder, encoder = load_or_create_encoder(input_vocab_size)
    train_encoder_aux(autoencoder, encoder, training_generator, eval_generator,
                      int(len(training_arr[0].reshape([-1, config.N_NEIGHBORS])) / config.CHUNK_SIZE1))
    return autoencoder, encoder


def load_or_create_encoder(x_vocab_size):
    if results.load_model1:
        print("Loading encoder model ...")
        autoencoder = load_model("autoencoder." + str(config.INPUT_VOCAB_SIZE) + "_" + str(
            config.HIDDEN_LAYER_SIZE) + "." + config.MODEL_FILE)
        encoder = load_model(
            "encoder." + str(config.INPUT_VOCAB_SIZE) + "_" + str(config.HIDDEN_LAYER_SIZE) + "." + config.MODEL_FILE)
    else:
        print("Creating encoder model ...")
        autoencoder, encoder = create_autoencoder(x_vocab_size)
    return autoencoder, encoder


########  LSTM for actual variable name output

def generate_sequence_for_lstm(encoder, arr, input_vocab_size, output_vocab_size):
    c = 0
    input = arr[0]
    output = arr[1]
    length = len(input)
    indices = list(range(length))
    np.random.shuffle(indices)
    input = input[indices, :]
    output = output[indices]

    while True:
        if (c + config.CHUNK_SIZE2 > length):
            np.random.shuffle(indices)
            input = input[indices, :]
            output = output[indices]
            c = 0
        i_slice = input[c:c + config.CHUNK_SIZE2, :]
        i_slice = np_utils.to_categorical(i_slice.reshape([-1]), num_classes=input_vocab_size).reshape(
            [-1, config.N_NEIGHBORS, input_vocab_size])
        i_encoded_slice = encoder.predict(i_slice)
        i_slice = i_encoded_slice.reshape([-1, config.SEQ_LEN, config.HIDDEN_LAYER_SIZE])
        o_slice = output[c:c + config.CHUNK_SIZE2]
        o_slice = np_utils.to_categorical(o_slice, num_classes=output_vocab_size)
        c = c + config.CHUNK_SIZE2
        print("i_slice.shape = {} o_slice.shape {}".format(i_slice.shape, o_slice.shape))
        yield (i_slice, o_slice)


def get_generators_for_lstm(encoder, training_arr, validation_arr, input_vocab_size, output_vocab_size):
    training_generator = generate_sequence_for_lstm(encoder, training_arr, input_vocab_size, output_vocab_size)
    eval_generator = generate_sequence_for_lstm(encoder, validation_arr, input_vocab_size, output_vocab_size)
    return (training_generator, eval_generator)


def create_lstm(output_vocab_size):
    inputs = Input(shape=(config.SEQ_LEN, config.HIDDEN_LAYER_SIZE))
    layer1 = LSTM(config.HIDDEN_LAYER_SIZE2, return_sequences=False)(inputs)
    layer2 = Dense(output_vocab_size, activation='softmax')(layer1)
    embedding = Model(inputs, layer1)
    lstm = Model(inputs, layer2)

    embedding.compile(loss="mse", optimizer="adam", metrics=["accuracy"])
    lstm.compile(loss="categorical_crossentropy", optimizer="adam", metrics=["accuracy"])
    embedding.summary()
    lstm.summary()
    return (embedding, lstm)


def load_or_create_lstm(output_vocab_size):
    if results.load_model2:
        print("Loading lstm model")
        embedding = load_model("embedding_" + str(config.HIDDEN_LAYER_SIZE2) + "_" + str(
            config.OUTPUT_VOCAB_SIZE) + "." + config.MODEL_FILE)
        lstm = load_model(
            "lstm_" + str(config.HIDDEN_LAYER_SIZE2) + "_" + str(config.OUTPUT_VOCAB_SIZE) + "." + config.MODEL_FILE)
    else:
        print("Creating lstm model with output vocab size {}".format(output_vocab_size))
        embedding, lstm = create_lstm(output_vocab_size)
    return embedding, lstm


def train_lstm_aux(embedding, lstm, training_generator, eval_generator, n_chunks, o_index2word):
    print("Starting lstm training ...")
    with easy_open("history.csv", "w") as myfile:
        myfile.write("")

    for i in range(config.NUM_EPOCHS):
        for j in range(min(n_chunks, 10)):
            # for j in range(n_chunks):
            train_data = next(training_generator)
            eval_data = next(eval_generator)
            print("Running epoch {} on data[{}] with train data shape being {} {}\n".format(i + 1, j + 1,
                                                                                            train_data[0].shape,
                                                                                            train_data[1].shape))
            history = lstm.fit(train_data[0], train_data[1], batch_size=config.BATCH_SIZE, epochs=1,
                               validation_data=(eval_data[0], eval_data[1]))

            with easy_open("history.csv", "a") as myfile:
                myfile.write("{} {} {} {}\n".format(history.history['loss'].pop(), history.history['acc'].pop(),
                                                    history.history['val_loss'].pop(),
                                                    history.history['val_acc'].pop()))
            if j % 10 == 0:
                embedding.save(easy_path("embedding_" + str(config.HIDDEN_LAYER_SIZE2) + "_" + str(
                    config.OUTPUT_VOCAB_SIZE) + "." + config.MODEL_FILE))
                lstm.save(easy_path("lstm_" + str(config.HIDDEN_LAYER_SIZE2) + "_" + str(
                    config.OUTPUT_VOCAB_SIZE) + "." + config.MODEL_FILE))


def train_lstm(encoder, training_arr, validation_arr, i_map, o_map):
    training_generator, eval_generator = get_generators_for_lstm(encoder, training_arr, validation_arr, i_map[0],
                                                                 o_map[0])
    embedding, lstm = load_or_create_lstm(o_map[0])
    train_lstm_aux(embedding, lstm, training_generator, eval_generator, int(len(training_arr[0]) / config.CHUNK_SIZE2),
                   o_map[2])


def load_and_train_lstm():
    training_arr, validation_arr, i_map, o_map = load_and_process_arrays()
    autoencoder, encoder = train_encoder(training_arr, validation_arr, i_map[0])
    train_lstm(encoder, training_arr, validation_arr, i_map, o_map)


class Results:
    def __init__(self):
        self.is_pload = False
        self.is_iload = False
        self.is_oload = False
        self.load_model1 = False
        self.load_model2 = False


if __name__ == "__main__":
    drive.mount('/content/gdrive')
    config = Config()
    with easy_open(config.CONFIG_FILE, 'w') as outfile:
        json.dump(config.__dict__, outfile)
    print("Running with configuration ...")
    print(json.dumps(config.__dict__, indent=4))

    results = Results()  # parse_args()
    load_and_train_lstm()
