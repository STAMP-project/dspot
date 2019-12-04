#!/usr/bin/env bash

DPSOT_VERSION=${1}
cp dspot/target/dspot-${DSPOT_VERSION}-jar-with-dependencies.jar /tmp
cd /tmp
java -jar dspot-${DSPOT_VERSION}-jar-with-dependencies.jar --example