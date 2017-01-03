#!/usr/bin/env bash

cd ..

git clone https://github.com/INRIA/spoon.git
cd spoon
mvn install -DskipTests

cd ..

git clone https://github.com/danglotb/sosiefier.git
cd sosiefier
git checkout spoon-version
mvn install

cd ..

git clone https://github.com/danglotb/diversify-profiling.git
cd diversify-profiling
git checkout spoon-version
mvn install

cd ../dspot