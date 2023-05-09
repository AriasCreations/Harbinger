#!/bin/bash

UID=$(id -uz)
if [ $UID -eq 0 ]
then
  echo "We are root. Processing harbinger setup commands"

  mkdir /harbinger
  useradd -m harbinger -s /bin/bash -d /harbinger
  chown -Rv harbinger:harbinger /harbinger
  cp ./harbinger_update.sh /bin/
  chmod +x /bin/harbinger_update.sh
  cp ./harbinger.service /etc/systemd/system/
  chown root:root /bin/harbinger_update.sh
  chown root:root /etc/systemd/system/harbinger.service

  systemctl daemon-reload
  systemctl enable harbinger
  cd ..
  ./gradlew build
  mv build/libs/Harbinger-*.jar /harbinger/server.jar
  chown harbinger:harbinger /harbinger/server.jar

  echo -ne "Harbinger has been set up\n\n** DO NOT RE-RUN THIS SCRIPT **\nUser added: harbinger\nGroup added: harbinger\nSystem Service Added: harbinger\nShell Script added: /bin/harbinger_update.sh\nService Enabled at Reboot: Harbinger\n\n"
else
  echo -ne "Admin access is required to set up Harbinger for the first time"
  sudo ./setup_harbinger.sh
fi