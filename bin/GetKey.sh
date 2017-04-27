#!/bin/bash

SCRIPT_DIR=$(dirname `which $0`)
LIB_DIR="${SCRIPT_DIR}/../target/lib/"

for i in ${LIB_DIR}*.jar; do
    CLASSPATH=$CLASSPATH:$i
done

$JAVA_HOME/bin/java mil.nga.util.GetKey $1 $2 $3 $4
