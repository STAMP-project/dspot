#!/usr/bin/env bash

DSPOT_VERSION=${1}

git clone https://github.com/STAMP-project/testrunner.git
cd testrunner
mvn install -DskipTests
cd ..

cd dspot-maven/src/test/resources/multi-module
mvn eu.stamp-project:dspot-maven:${DSPOT_VERSION}:amplify-unit-tests -Dtarget-module=module -Dverbose -Damplifiers=FastLiteralAmplifier -Diteration=1 -Dtest=example.TestSuiteExample -Dtest-criterion=JacocoCoverageSelector

if [ -f target/trash/example/TestSuiteExample.java ]; then
    exit 0
else
    exit 1
fi