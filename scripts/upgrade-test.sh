#!/bin/bash
set -ex
MVP_UPGRADE_FROM_TAG=$1
MVP_UPGRADE_TO_TAG=$2
TEST_HOST=localhost

# Values in seconds
MAX_WAIT_FOR_MVP_APPLICATION=120
MAX_WAIT_FOR_AMQP_MESSAGES=120

if [ ! -z "$3" ]; then
	TEST_HOST=$3
fi

echo "Upgrade from: $MVP_UPGRADE_FROM_TAG"
echo "Upgrade to  : $MVP_UPGRADE_TO_TAG"
echo "Test Host   : $TEST_HOST"
echo ""

function check_application_startup () {
	APPLICATION=$1
	slept=0
	while [[ "$(docker inspect --format='{{.State.Health.Status}}' mvp_${APPLICATION}_1)" = 'starting' &&  "$(docker inspect --format='{{.State.Status}}' mvp_${APPLICATION}_1)" != 'exited' ]]; do
		slept=$((slept+1))
		echo -n "."
		if [ $slept -gt $MAX_WAIT_FOR_MVP_APPLICATION ]; then
			echo ""
			docker-compose logs ${APPLICATION}
			echo ":: Bailing out after $MAX_WAIT_FOR_MVP_APPLICATION seconds.."
			exit 1
		fi
		sleep 1
	done
	docker-compose logs ${APPLICATION}
	healthStatus=$(docker inspect --format='{{.State.Health.Status}}' mvp_${APPLICATION}_1)
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
MVP_TAG=$MVP_UPGRADE_FROM_TAG docker-compose up -d rabbitmq application geoservice
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

MVP_TAG=$MVP_UPGRADE_TO_TAG docker-compose up -d application geoservice
echo -n ":: Waiting for new instance of MVP application to start up"
check_application_startup application

echo -n ":: Waiting for new instance of GEOSERVICE application to start up"
check_application_startup geoservice

echo ":: Upgrade successfully completed!"
