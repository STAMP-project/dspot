#!/usr/bin/env bash

DSPOT_VERSION=${1}

cd dspot-maven/src/test/resources/multi-module
mvn eu.stamp-project:dspot-maven:${DSPOT_VERSION}:amplify-unit-tests -Dverbose -Dpath-to-properties=multi-module.properties -Damplifiers=TestDataMutator -Diteration=1 -Dtest=example.TestSuiteExample -Dtest-criterion=JacocoCoverageSelector

if [ -f target/dspot/output/example/TestSuiteExample.java ]; then
    exit 0
else
    exit 1
fi