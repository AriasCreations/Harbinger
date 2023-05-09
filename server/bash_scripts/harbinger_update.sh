#!/bin/bash


WHOAMI=$(id -unz)

if [ $WHOAMI == "harbinger" ]
then
  cd "$1"
  echo "Please wait... Performing CI/CD update"
  PSK=$(<~/.harbinger_psk)
  PORT=7768
  echo "Moving Harbinger Update into place"
  mv build/libs/Harbinger-*.jar /harbinger/server.jar
  curl -d "{\"psk\":\"$PSK\"}" http://127.0.0.1:$PORT/stop
  echo "Allowing Harbinger to exit and be automatically restarted"

  echo "Updating service files..."
  cp bash_scripts/harbinger.service /tmp/harbinger_service_script
  cp bash_scripts/harbinger_update.sh /tmp/harbinger_updater
  cp bash_scripts/pesc_harbinger_update_runner.sh /tmp/harbinger_updater2
  pesc_harbinger_update
else
  su -l harbinger -c "$(which harbinger_update) \"$(pwd)\""
fi
