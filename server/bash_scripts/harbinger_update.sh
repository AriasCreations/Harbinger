#!/bin/bash

echo "Please wait... Performing CI/CD update"
PSK="change_me"
PORT=7768
echo "Moving Harbinger Update into place"
mv build/libs/Harbinger-*.jar /harbinger/server.jar
curl -d "{\"psk\":\"$PSK\"}" http://127.0.0.1:$PORT/stop
echo "Allowing Harbinger to exit and be automatically restarted"
