#!/bin/bash

UserID=$(id -uz)
if [ $UserID -eq 0 ]
then
  echo "We are root. Processing harbinger setup commands"

  mkdir /harbinger
  useradd -m harbinger -s /bin/bash -d /harbinger
  chown -Rv harbinger:harbinger /harbinger
  cp ./harbinger_update.sh /bin/
  chmod +x /bin/harbinger_update.sh
  cp ./harbinger.service /etc/systemd/system/
  chown root:root /bin/harbinger_update
  chown root:root /etc/systemd/system/harbinger.service
  mv ./pesc_harbinger_update_runner.sh /bin/pesc_harbinger_update_runner
  chmod +x /bin/pesc_harbinger_update_runner
  chown root:root /bin/pesc_harbinger_update_runner

  systemctl daemon-reload
  systemctl enable harbinger
  cd ../..
  ./gradlew build
  cd server
  mv build/libs/Harbinger-*.jar /harbinger/server.jar
  chown harbinger:harbinger /harbinger/server.jar
  cd bash_scripts


  gcc -c privesc.c -o privesc.o
  g++ privesc.o -o /bin/pesc_harbinger_update
  chown root:root /bin/pesc_harbinger_update
  chmod u=rwx,g=rx,+s /bin/pesc_harbinger_update

  passwd -d harbinger

  echo -ne "Harbinger has been set up\n\n** DO NOT RE-RUN THIS SCRIPT **\nUser added: harbinger\nGroup added: harbinger\nSystem Service Added: harbinger\nShell Script added: /bin/harbinger_update\nService Enabled at Reboot: Harbinger\nBinary Added: /bin/pesc_harbinger_update\nShell Script added: /bin/pesc_harbinger_update_runner\n\n"
else
  echo -ne "Admin access is required to set up Harbinger for the first time"
  sudo ./setup_harbinger.sh
fi