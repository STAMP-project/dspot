# post-dspot

Post-dspot processes [DSpot](http://github.com/STAMP-project/dspot.git)'s output to make it readable.

The process has 3 steps: 

1. Minimize the test method by removing all the "useless" statements.
2. Rename the test method. This is done use [code2vec](https://github.com/tech-srl/code2vec).
3. Rename the local variable used in the the test method.

## Install