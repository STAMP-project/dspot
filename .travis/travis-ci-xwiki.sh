#!/usr/bin/env bash

DPSOT_VERSION=${1}

cd dspot
git clone https://github.com/xwiki/xwiki-commons.git
cd xwiki-commons
git reset --hard fad7990ee27aa6de039412587fc9546b90845e34
mvn clean install -DskipTests --quiet
cd ..

pwd
ls
ls xwiki-commons

java -jar target/dspot-${DSPOT_VERSION}-jar-with-dependencies.jar --path-to-properties src/test/resources/xwiki.properties --descartes --verbose --generate-new-test-class --test org.xwiki.xml.internal.html.DefaultHTMLCleanerTest

if [ -f dspot-out/xwiki-commons/org/xwiki/xml/internal/html/AmplDefaultHTMLCleanerTest.java ]; then
    exit 0
else
    exit 1
fi
