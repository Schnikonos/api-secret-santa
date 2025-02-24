#!/usr/bin/env bash

rm -rf src/main/resources/static
rm -rf target/classes/static
mkdir -p target/classes

export FOLDER_PATH="${HOME}/Work/other/santa/santa-app"
cp -r ${FOLDER_PATH}/build src/main/resources/static
cp -r ${FOLDER_PATH}/build target/classes/static

