#!/usr/bin/env bash

cd dspot && mvn clean test jacoco:report coveralls:report
