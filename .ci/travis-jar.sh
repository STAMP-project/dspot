#!/usr/bin/env bash

DPSOT_VERSION=${1}
cd dspot && java -jar target/dspot-${DSPOT_VERSION}-jar-with-dependencies.jar --example