#!/usr/bin/env bash

DPSOT_VERSION=${1}

cd dspot-diff-test-selection/src/test/resources/tavern

mvn clean eu.stamp-project:dspot-diff-test-selection:${DSPOT_VERSION}:list -Dpath-dir-second-version="../tavern-refactor" eu.stamp-project:dspot-maven:${DSPOT_VERSION}:amplify-unit-tests -Dpath-to-test-list-csv=testsThatExecuteTheChange.csv -Dverbose -Dtest-criterion=ChangeDetectorSelector -Dpath-to-properties=src/main/resources/tavern.properties -Damplifiers=NumberLiteralAmplifier -Diteration=2
