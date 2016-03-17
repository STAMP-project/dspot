#!/bin/sh

mvn package

cd example-commons-collections
git clone https://github.com/apache/commons-collections.git
cd ..
java -Xmx2g -cp target/dspot-1.0.0-jar-with-dependencies.jar fr.inria.diversify.dspot.DSpot example.properties