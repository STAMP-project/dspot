#!/usr/bin/env bash

# this script is meant to be run from dspot module
# this script clean the .class inside the folder src/main/resources
# and copy the new one from target/classes
# this is done to have matched version with sources and compiled resources

rm -rf src/main/resources/compare/
rm -rf src/main/resources/listener/
mkdir --parent src/main/resources/listener/
cp -r target/classes/fr/inria/diversify/compare/ src/main/resources/compare/
cp target/classes/fr/inria/stamp/test/listener/TestListener.class src/main/resources/listener/TestListener.class