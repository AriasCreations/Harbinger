#!/bin/bash


WHOAMI=$(id -unz)

if [ $WHOAMI == "harbinger" ]
then
  cd "$1"
  echo "Please wait... Performing CI/CD update"
  PSK=$(<~/.harbinger_psk)
  PORT=7768
  echo "Moving Harbinger Update into place"
  cp build/libs/Harbinger-*.jar /tmp/harbinger_server.jar
  echo "Allowing Harbinger to exit and be automatically restarted"

  echo "Updating service files..."
  cp bash_scripts/harbinger.service /tmp/harbinger_service_script
  cp bash_scripts/harbinger_update.sh /tmp/harbinger_updater
  cp bash_scripts/pesc_harbinger_update_runner.sh /tmp/harbinger_updater2
  cp bash_scripts/runHarbinger.sh /tmp/run_harbinger_script
  cp bash_scripts/runHarbingerStage2.sh /tmp/run_harbinger_script2
  cp bash_scripts/stopHarbinger.sh /tmp/stop_harbinger_script

  git --no-pager log > /tmp/harbinger_patch

  curl -d "{\"psk\":\"$PSK\",\"type\":\"update_critical_info\"}" http://127.0.0.1:$PORT/api



  echo -ne $PSK >/tmp/.harbinger_psk
  echo -ne $PORT >/tmp/.harbinger_port
  pesc_harbinger_update
else
  su -l harbinger -c "$(which harbinger_update) \"$(pwd)\""
fi
