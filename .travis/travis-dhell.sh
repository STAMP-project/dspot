#!/usr/bin/env bash

DPSOT_VERSION=${1}

cd dspot
git clone https://github.com/STAMP-project/dhell.git

java -jar target/dspot-${DSPOT_VERSION}-jar-with-dependencies.jar --absolute-path-to-project-root dhell/ --iteration 1 --amplifiers MethodAdd --test eu.stamp_project.examples.dhell.HelloAppTest

if [ -f dspot-out/eu/stamp_project/examples/dhell/HelloAppTest.java ]; then
    exit 0
else
    exit 1
fi