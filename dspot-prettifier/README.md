# post-dspot

Post-dspot processes [DSpot](http://github.com/STAMP-project/dspot.git)'s output to make it readable.

The process has 3 steps: 

1. Minimize the test method by removing all the "useless" statements.
2. Rename the test method. This is done using [code2vec](https://github.com/tech-srl/code2vec).
3. Rename the local variable used in the the test method. This is done based on [context2name](https://github.com/rbavishi/Context2Name).

## Install
For __code2vec__, you need to run `install_code2vec.sh` to download repo.

## Info
For __context2name__, if you want to train one better model:

1. Prepare corpus. You could run `PGA.java` directly or make some adjustments to customize corpus.
2. Process corpus. You just need to call `fnCorpus()` of `C2N.java`, then `training.csv` and `validation.csv` get generated.
3. Train model. You just need to run `c2n_train.py` with `training.csv`, `validation.csv` and `config.json` at hand.
4. Run one demo. You just need to call `fnDemo()` of `C2N.java`.
