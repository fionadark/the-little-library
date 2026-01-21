#!/bin/bash
exec java -Dserver.port=$PORT -jar build/libs/app.jar
