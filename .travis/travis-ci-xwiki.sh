#!/usr/bin/env bash

DPSOT_VERSION=${1}

cd dspot
git clone https://github.com/xwiki/xwiki-commons.git
cd xwiki-commons
git reset --hard af3f542acc23e4d70cf61b4f6ac9d261a2a75bbf
mvn clean install -DskipTests --quiet
cd ..

mvn eu.stamp-project:dspot-maven:${DSPOT_VERSION}:amplify-unit-tests -Dgenerate-new-test-class=true -Dtest=org.xwiki.component.ProviderTest -Dtarget-module=xwiki-commons-core/xwiki-commons-component/xwiki-commons-component-default

cp target/dspot/output/org/xwiki/component/AmplProviderTest.java xwiki-commons-core/xwiki-commons-component/xwiki-commons-component-default/src/test/java/org/xwiki/component/

cd xwiki-commons-core/xwiki-commons-component/xwiki-commons-component-default

mvn clean test