#!/bin/bash

# Do not execute this by itself!
PORT=$1
PSK=$2
mv /tmp/harbinger_service_script /etc/systemd/system/harbinger.service
mv /tmp/harbinger_updater /bin/harbinger_update
mv /tmp/harbinger_updater2 /bin/pesc_harbinger_update_runner
mv /tmp/harbinger_server.jar /harbinger/server.jar


chown harbinger:harbinger /harbinger/server.jar

chown root:root /bin/pesc_harbinger_update_runner
chmod +x /bin/pesc_harbinger_update_runner

chown root:root /bin/harbinger_update
chmod +x /bin/harbinger_update

chown root:root /etc/systemd/system/harbinger.service

systemd daemon-reload
systemd reload harbinger
curl -d "{\"psk\":\"$PSK\"}" http://127.0.0.1:$PORT/stop