#!/usr/bin/env bash

set -e

source /opt/jdk_switcher/jdk_switcher.sh

jdk_switcher use openjdk8

# see doIntegrationTests=true -> see https://stackoverflow.com/a/15881238
mvn -Djava.src.version=1.8 test -DdoIntegrationTests=true -f dspot/pom.xml
