#!/usr/bin/env bash

cd ..

git clone https://github.com/danglotb/sosiefier.git
cd sosiefier
git checkout spoon-version
mvn install

cd ../dspot