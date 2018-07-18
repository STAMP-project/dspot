#!/usr/bin/env bash

source /opt/jdk_switcher/jdk_switcher.sh

jdk_switcher use openjdk8 & mvn -Djava.src.version=1.8 test