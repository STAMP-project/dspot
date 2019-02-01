#!/usr/bin/env bash

DPSOT_VERSION=${1}

cd dspot-diff-test-selection

./src/main/bash/setup-tavern.sh

cd tavern

git diff refactor > patch.diff

mvn clean eu.stamp-project:dspot-diff-test-selection:${DSPOT_VERSION}:list -DpathToDiff="patch.diff" -DpathToOtherVersion="../tavern-refactor" eu.stamp-project:dspot-maven:${DSPOT_VERSION}:amplify-unit-tests -Dpath-to-test-list-csv=testsThatExecuteTheChange.csv -Dverbose -Dtest-criterion=ChangeDetectorSelector -Dpath-to-properties=src/main/resources/tavern.properties -Damplifiers=NumberLiteralAmplifier -Diteration=2
