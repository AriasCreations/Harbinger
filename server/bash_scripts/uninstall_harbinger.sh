#!/bin/bash

UID=$(id -uz)
if [ $UID -eq 0 ]
then
  echo "Please wait while we uninstall Harbinger Server\n\n* ALERT * Data files will not be deleted"

  systemctl disable harbinger
  systemctl stop harbinger
  userdel harbinger
  groupdel harbinger
  rm /bin/pesc_harbinger_update
  rm /bin/pesc_harbinger_update_runner

  rm /bin/harbinger_update
  rm /etc/systemd/system/harbinger.service

  echo "Uninstallation completed. You may need to manually review what files are remaining in /harbinger"
  rm /harbinger/server.jar
else
  sudo ./uninstall_harbinger.sh
fi