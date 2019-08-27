#!/usr/bin/env bash

if [[ $TASK == "test-rosetta" ]]
then
    echo "Testing Rosetta PR"
    $TRAVIS_BUILD_DIR/gradlew clean test integrationTests
elif [[ $TASK == "spotless" ]]
then
    echo "Checking code style with spotless"
    $TRAVIS_BUILD_DIR/gradlew spotlessJavaCheck
else
    echo "I do not understand TASK = ${TASK}"
    echo "TASK must be either test-rosetta or spotless"
fi