#!/usr/bin/env bash

rm -rf src/main/resources/static
rm -rf target/classes/static
mkdir -p target/classes

cp -r ~/work/santa/santa-app/build src/main/resources/static
cp -r ~/work/santa/santa-app/build target/classes/static

