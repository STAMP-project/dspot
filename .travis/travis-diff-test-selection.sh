#!/usr/bin/env bash

cd dspot-diff-test-selection

# retrieve submodules
git pull --recurse-submodules && git submodule update --recursive

# build plugin and install it
mvn install

# setup commons-math project
./src/main/bash/setup-commons-math.sh

# execute the plugin
cd commons-math && mvn clean eu.stamp-project:diff-test-selection:list -DpathToDiff=".bugs-dot-jar/developer-patch.diff" -DpathToOtherVersion="../commons-math_fixed"
