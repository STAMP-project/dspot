#!/usr/bin/env bash

DPSOT_VERSION=${1}

cd dspot-diff-test-selection/src/test/resources/tavern

mvn clean eu.stamp-project:dspot-diff-test-selection:${DSPOT_VERSION}:list -Dpath-dir-second-version="../tavern-refactor" eu.stamp-project:dspot-maven:${DSPOT_VERSION}:amplify-unit-tests -Dpath-to-test-list-csv=testsThatExecuteTheChange.csv -Dverbose -Dtest-criterion=ChangeDetectorSelector -Damplifiers=NumberLiteralAmplifier -Diteration=2 -Dabsolute-path-to-second-version=../tavern-refactor

if [ -f target/dspot/output/fr/inria/stamp/MainTest.java ]; then
    echo "SUCCESS"
else
    exit 1
fi

rm -rf target/dspot

mvn clean eu.stamp-project:dspot-diff-test-selection:${DSPOT_VERSION}:list -Dpath-dir-second-version="../tavern-refactor" -Dpath-to-diff=patch.diff eu.stamp-project:dspot-maven:${DSPOT_VERSION}:amplify-unit-tests -Dpath-to-test-list-csv=testsThatExecuteTheChange.csv -Dverbose -Dtest-criterion=ChangeDetectorSelector -Dabsolute-path-to-second-version=../tavern-refactor -Damplifiers=NumberLiteralAmplifier -Diteration=2

if [ -f target/dspot/output/fr/inria/stamp/MainTest.java ]; then
    exit 0
else
    exit 1
fi
