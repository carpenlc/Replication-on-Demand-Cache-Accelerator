#!/bin/bash

for i in ../target/lib/*.jar; do
    CLASSPATH=$CLASSPATH:$i
done

$JAVA_HOME/bin/java mil.nga.rod.accelerator.CacheManager
