#!/usr/bin/env bash

DPSOT_VERSION=${1}

cd dspot-diff-test-selection

# retrieve submodules
git clone https://github.com/bugs-dot-jar/commons-math.git

# build plugin and install it
mvn install

# setup commons-math project
./src/main/bash/setup-commons-math.sh

# execute the plugin
cd commons-math && mvn clean eu.stamp-project:dspot-diff-test-selection:${DSPOT_VERSION}:list -DpathToDiff=".bugs-dot-jar/developer-patch.diff" -DpathToOtherVersion="../commons-math_fixed"
