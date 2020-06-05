#!/usr/bin/env bash

source /opt/jdk_switcher/jdk_switcher.sh

./dspot-prettifer/src/test/bash/install_code2vec.sh

pip3 install --quiet bottleneck numpy keras tensorflow

jdk_switcher use openjdk8 & mvn -Djava.src.version=1.8 test -f dspot-prettifier/pom.xml
