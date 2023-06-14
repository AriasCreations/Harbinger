#!/bin/bash

# Do not execute this by itself!
PORT=$(</tmp/.harbinger_port)
PSK=$(</tmp/.harbinger_psk)

rm /tmp/.harbinger_port
rm /tmp/.harbinger_psk


mv /tmp/harbinger_service_script /etc/systemd/system/harbinger.service
mv /tmp/harbinger_updater /bin/harbinger_update
mv /tmp/harbinger_updater2 /bin/pesc_harbinger_update_runner
mv /tmp/harbinger_server.jar /harbinger/server.jar
mv /tmp/run_harbinger_script /usr/bin/runHarbinger


chown harbinger:harbinger /harbinger/server.jar

chown root:root /bin/pesc_harbinger_update_runner
chmod +x /bin/pesc_harbinger_update_runner
chmod +x /usr/bin/runHarbinger

chown root:root /bin/harbinger_update
chmod +x /bin/harbinger_update

chown root:root /etc/systemd/system/harbinger.service

systemctl daemon-reload


curl -d "{\"psk\":\"$PSK\"}" http://127.0.0.1:$PORT/stop
sleep 2
systemctl restart harbinger