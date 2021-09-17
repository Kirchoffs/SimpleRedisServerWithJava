#!/bin/sh
set -e
tmpFile=$(mktemp -d)
javac -sourcepath src/main/java src/main/java/RedisServer.java -d "$tmpFile"
jar cf RedisServer.jar -C "$tmpFile"/ .
exec java -cp RedisServer.jar RedisServer "$@"
