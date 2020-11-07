#!/usr/bin/env bash

cd dspot && mvn -DTRAVIS_JOB_ID=$TRAVIS_JOB_ID -DdoIntegrationTests=true clean test
