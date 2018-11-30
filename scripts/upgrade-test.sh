#!/bin/bash
set -e
MVP_UPGRADE_FROM_TAG=
MVP_UPGRADE_TO_TAG=
TEST_HOST=
APM_ENV=

# Values in seconds
MAX_WAIT_FOR_MVP_APPLICATION=120
MAX_WAIT_FOR_AMQP_MESSAGES=120

if [ -z "$1" ]; then
	MVP_UPGRADE_FROM_TAG="production"
else
	MVP_UPGRADE_FROM_TAG=$1
fi

if [ -z "$2" ]; then
	MVP_UPGRADE_TO_TAG="develop"
else
	MVP_UPGRADE_TO_TAG=$2
fi

if [ ! -z "$3" ]; then
	TEST_HOST=$3
else
	TEST_HOST="localhost"
fi

if [ ! -z "$4" ]; then
	APM_ENV=$4
else
	APM_ENV="local-upgrade-test"
fi

echo "Upgrade from: $MVP_UPGRADE_FROM_TAG"
echo "Upgrade to  : $MVP_UPGRADE_TO_TAG"
echo "Test Host   : $TEST_HOST"
echo "APM Env     : $APM_ENV"
echo ""

function check_application_startup () {
	APPLICATION=$1
	slept=0
	containerId=$(docker ps|grep "mvp_${APPLICATION}_1"| awk '{ print $1 }')

	while [ -z ${containerId} ]; do
		slept=$((slept+1))
		if [ $slept -gt $MAX_WAIT_FOR_MVP_APPLICATION ]; then
			docker ps -a
			echo ":: Bailing out after $MAX_WAIT_FOR_MVP_APPLICATION seconds.."
			exit 1
		fi
		echo "Try: ${slept}, No container name mvp_${APPLICATION}_1 yet, sleeping for 1 second"
		sleep 1
		containerId=$(docker ps|grep mvp_${APPLICATION}_1| awk '{ print $1 }')
	done

	healthStatus=$(docker inspect --format='{{.State.Health.Status}}' ${containerId})
	status=$(docker inspect --format='{{.State.Status}}' ${containerId})
	while [[ "$healthStatus" = 'starting' || "$healthStatus" = 'unhealthy' ]] && [[ "$status" != 'exited' ]]; do
		slept=$((slept+1))
		echo -n "."
		if [ $slept -gt $MAX_WAIT_FOR_MVP_APPLICATION ]; then
			echo ""
			docker logs ${containerId}
			docker inspect ${containerId}
			echo ":: Bailing out after $MAX_WAIT_FOR_MVP_APPLICATION seconds.."
			exit 1
		fi
		sleep 1
		healthStatus=$(docker inspect --format='{{.State.Health.Status}}' ${containerId})
		status=$(docker inspect --format='{{.State.Status}}' ${containerId})
	done
	docker logs ${containerId}
	docker inspect ${containerId}
	healthStatus=$(docker inspect --format='{{.State.Health.Status}}' ${containerId})
	test "${healthStatus}" = 'healthy' || (echo ${healthStatus} && exit 1)
}

function check_rabbitmq_startup () {
	slept=0
	CMD="curl -u guest:guest -sL -w %{http_code}\\n http://$TEST_HOST:15672/api/aliveness-test/%2F -o /dev/null"
	while [[ "$($CMD)" != "200" ]]; do
		slept=$((slept+1))
		echo -n "."
		if [ $slept -gt $MAX_WAIT_FOR_MVP_APPLICATION ]; then
			echo ""
			echo ":: Bailing out after $MAX_WAIT_FOR_MVP_APPLICATION seconds.."
			exit 1
		fi
		sleep 1
	done
}

function check_amqp_message_arrived () {
slept=0
while [[ "x$(curl -s -u mvpadmin@elvaco.se:changeme $TEST_HOST:8080/api/v1/meters | jq '.totalElements')" != "x1" ]]; do
	slept=$((slept+1))
	echo -n "."
	if [ $slept -gt $MAX_WAIT_FOR_AMQP_MESSAGES ]; then
		echo ""
		echo ":: Bailing out after $MAX_WAIT_FOR_AMQP_MESSAGES seconds.."
		exit 1
	fi
	sleep 1
done
}

function send_amqp_message () {
	amqp-publish -u amqp://guest:guest@$TEST_HOST:5672 -r 'MVP' -C application/json < upgrade-test/messages/new-measurements.json
}

# === Main application ===
APM_ENV=$APM_ENV MVP_TAG=$MVP_UPGRADE_FROM_TAG docker-compose up -d rabbitmq application geoservice
echo -n ":: Waiting for GEOSERVICE application to start up"
check_application_startup geoservice

echo -n ":: Waiting for MVP application to start up"
check_application_startup application

echo -n ":: Waiting for RabbitMQ application to start up"
check_rabbitmq_startup

echo ""
echo ":: Sending AMQP message"
send_amqp_message
echo ":: Message sent"

echo -n ":: Checking if the message arrived"
check_amqp_message_arrived
echo ""
echo ":: Message received"

echo ":: Stopping old MVP application"
docker-compose stop application

echo ":: Stopping old GEOSERVICE application"
docker-compose stop geoservice

APM_ENV=$APM_ENV MVP_TAG=$MVP_UPGRADE_TO_TAG docker-compose up -d application geoservice
echo -n ":: Waiting for new instance of MVP application to start up"
check_application_startup application

echo -n ":: Waiting for new instance of GEOSERVICE application to start up"
check_application_startup geoservice

echo ":: Upgrade successfully completed!"
