#!/usr/bin/env bash

cd dspot && mvn -DTRAVIS_JOB_ID=$TRAVIS_JOB_ID -DdoIntegrationTests=true -Dorg.slf4j.simpleLogger.defaultLogLevel=error clean test 
