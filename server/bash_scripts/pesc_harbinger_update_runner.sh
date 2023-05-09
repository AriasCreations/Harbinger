#!/bin/bash

# Do not execute this by itself!

mv /tmp/harbinger_service_script /etc/systemd/system/harbinger.service
mv /tmp/harbinger_updater /bin/harbinger_update
mv /tmp/harbinger_updater2 /bin/pesc_harbinger_update_runner

chown root:root /bin/pesc_harbinger_update_runner
chmod +x /bin/pesc_harbinger_update_runner

chown root:root /bin/harbinger_update
chmod +x /bin/harbinger_update

chown root:root /etc/systemd/system/harbinger.service

systemd daemon-reload
systemd reload harbinger