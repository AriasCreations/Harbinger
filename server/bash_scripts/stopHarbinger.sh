#!/bin/bash

PORT=7768
PSK=$(<~/.harbinger_psk)

curl -d "{\"psk\":\"$PSK\"}" http://127.0.0.1:$PORT/stop