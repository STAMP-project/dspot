#!/usr/bin/env bash

cd dspot
git clone https://github.com/STAMP-project/dhell.git
cd dhell

java -jar ../target/dspot-1.2.1-SNAPSHOT-jar-with-dependencies.jar --path-to-properties dhell.dspot --iteration 1 --amplifiers MethodAdd --test eu.stamp_project.examples.dhell.HelloAppTest

if [ -f dspot-out/eu/stamp_project/examples/dhell/HelloAppTest.java ]; then
    exit 0
else
    exit 1
fi