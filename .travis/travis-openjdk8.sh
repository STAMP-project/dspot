#!/usr/bin/env bash

set -e

# see doIntegrationTests=true -> see https://stackoverflow.com/a/15881238
mvn -Djava.src.version=1.8 -DdoIntegrationTests=true test -f dspot/pom.xml
