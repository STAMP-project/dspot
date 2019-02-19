#!/usr/bin/env bash

cd dspot && mvn -Pcoveralls -DTRAVIS_JOB_ID=$TRAVIS_JOB_ID clean test -Dtest=eu.stamp_project.dspot.amplifier.CharacterLiteralAmplifierTest jacoco:report coveralls:report -DrepoToken=${2}