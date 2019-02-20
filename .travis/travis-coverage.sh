#!/usr/bin/env bash

cd dspot && mvn -Pcoveralls -DTRAVIS_JOB_ID=$TRAVIS_JOB_ID clean test jacoco:report coveralls:report