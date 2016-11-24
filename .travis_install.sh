#!/usr/bin/env bash

cd ..

git clone https://github.com/DIVERSIFY-project/sosiefier.git
cd sosiefier
mvn install

git clone https://github.com/DIVERSIFY-project/profiling.git
cd profiling
mvn install

cd ../dspot