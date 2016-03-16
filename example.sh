#!/bin/sh

mvn package

mkdir example-commons-collections
cd example-commons-collections
git clone https://github.com/apache/commons-collections.git

java -Xmx2g