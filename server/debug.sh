#!/bin/bash

java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 $1
