#!/usr/bin/env bash

cd ..

git clone https://github.com/DIVERSIFY-project/sosiefier.git
cd sosiefier
mvn install

cd ..

git clone https://github.com/DIVERSIFY-project/diversify-profiling.git
cd diversify-profiling
mvn install

cd ../dspot