import argparse
import json
import pickle
import subprocess
import bottleneck
import numpy as np

from keras.utils import np_utils
from keras.models import load_model


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
        self.NUM_EPOCHS = 10  # 50
        self.ACCURACY = 0.8  # 0.996
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
    return '../model/' + file_path


def easy_open(file_path, mode):
    return open(easy_path(file_path), mode)


def load_file_from_web(url):
    command = 'cd ../model;wget -q -nc ' + url
    subprocess.call(command, shell=True)


def cat_files(file_name):
    prefix, postfix = 'sub_', '_'
    command = 'cd ../model;cat ' + prefix + file_name + postfix + '* > ' + file_name
    subprocess.call(command, shell=True)


def split_files(file_name):
    prefix, postfix = 'sub_', '_'
    command = 'cd ../model;split -b 90m ' + file_name + ' ' + prefix + file_name + postfix
    subprocess.call(command, shell=True)


class Handler:
    def __init__(self, imap, omap, encoder, lstm):
        self.imap = imap
        self.omap = omap
        self.encoder = encoder
        self.lstm = lstm

    def parse_input(self, inp):
        contexts = []
        targets = []
        for line in inp:
            tokens = line.split()
            target = tokens[1]
            context = tokens[2:]

            def translator(x):
                if x.startswith("1ID:-1"): return x.split(':')[2]
                if x.startswith("1ID:0"): return x.split(':')[2]
                if x.startswith("1ID"): return "1ID"
                return x

            context = list(map(translator, context))
            req = config.SEQ_LEN * config.N_NEIGHBORS
            if len(context) < req:
                context += [config.PAD_TOKEN] * req

            context = context[:req]
            context.reverse()
            contexts.append(context)
            targets.append(target)

        return contexts, targets

    def prepare_input(self, inp):
        d = self.imap[1][config.UNKNOWN_TOKEN]
        ctxs = []
        for ctx in inp[0]:
            ctxs.append(list(map(lambda x: self.imap[1].get(x, d), ctx)))
        return np.array(ctxs), inp[1]

    def prepare_output(self, out):
        return list(
            map(lambda y: list(map(lambda x: (-x[0], self.omap[2].get(x[1], config.UNKNOWN_TOKEN), x[2]), y)), out))

    def predict(self, inp):
        ctx, o = self.prepare_input(self.parse_input(inp))
        encoder_inp = np_utils.to_categorical(ctx.reshape([-1]), num_classes=self.imap[0]).reshape(
            [-1, config.N_NEIGHBORS, self.imap[0]])
        encoder_out = self.encoder.predict(encoder_inp)
        lstm_inp = encoder_out.reshape([-1, config.SEQ_LEN, config.HIDDEN_LAYER_SIZE])
        prediction = self.lstm.predict(lstm_inp)
        toptens = bottleneck.argpartition(-prediction, 10, axis=1)[:, :10]
        toptens = [sorted([(-float(prediction[i][int(j)]), int(j), i) for j in x]) for i, x in enumerate(toptens)]

        res = self.prepare_output(toptens)
        return res, o

    def handle(self, data):
        loaded_data = json.loads(data)
        dumped_data = json.dumps(self.predict(loaded_data))
        return dumped_data


if __name__ == "__main__":
    config = Config()
    parser = argparse.ArgumentParser()
    parser.add_argument('--differentiate-toplevel', action='store_true', default=False,
                        help='Treat vars with scope-id = 0 different from scope-id = -1')

    i_map_default = "i_" + str(config.INPUT_VOCAB_SIZE) + "_" + config.PROCESSED_FILE
    parser.add_argument('-i', type=str, default=i_map_default,
                        dest='iload',
                        help='Input vocabulary file')

    o_map_default = "o_" + str(config.OUTPUT_VOCAB_SIZE) + "_" + config.PROCESSED_FILE
    parser.add_argument('-o', type=str, default=o_map_default,
                        dest='oload',
                        help='Output vocabulary file')

    encoder_default = "encoder." + str(config.INPUT_VOCAB_SIZE) + "_" + str(
        config.HIDDEN_LAYER_SIZE) + "." + config.MODEL_FILE
    parser.add_argument('-e', type=str, default=encoder_default,
                        dest='encoder',
                        help='Encoder file')

    lstm_default = "lstm_" + str(config.HIDDEN_LAYER_SIZE2) + "_" + str(
        config.OUTPUT_VOCAB_SIZE) + "." + config.MODEL_FILE
    parser.add_argument('-m', type=str, default=lstm_default,
                        dest='lstm',
                        help='LSTM Model file')

    data_default = ""
    parser.add_argument('-d', type=str, default=data_default,
                        dest='data',
                        help='Data')

    load_file_from_web("https://github.com/STAMP-project/data/releases/download/v0.1.0/lstm_350_60000.model.h5")
    # cat_files(lstm_default)
    # split_files(lstm_default)

    args = parser.parse_args()
    imap = pickle.load(easy_open(args.iload, 'rb'))
    omap = pickle.load(easy_open(args.oload, 'rb'))
    encoder = load_model(easy_path(args.encoder))
    lstm = load_model(easy_path(args.lstm))

    print(Handler(imap, omap, encoder, lstm).handle(args.data))
