#!/bin/bash
set -e
MVP_UPGRADE_FROM_TAG=$1
MVP_UPGRADE_TO_TAG=$2
TEST_HOST=localhost

# Values in seconds
MAX_WAIT_FOR_MVP_APPLICATION=60
MAX_WAIT_FOR_AMQP_MESSAGES=60

if [ ! -z "$3" ]; then
	TEST_HOST=$3
fi

echo "Upgrade from: $MVP_UPGRADE_FROM_TAG"
echo "Upgrade to  : $MVP_UPGRADE_TO_TAG"
echo "Test Host   : $TEST_HOST"
echo ""

function check_application_startup () {
	slept=0
	while [[ "$(docker inspect --format='{{.State.Health.Status}}' mvp_application_1)" = 'starting' &&  "$(docker inspect --format='{{.State.Status}}' mvp_application_1)" != 'exited' ]]; do
		slept=$((slept+1))
		echo -n "."
		if [ $slept -gt $MAX_WAIT_FOR_MVP_APPLICATION ]; then
			echo ""
			echo ":: Bailing out after $MAX_WAIT_FOR_MVP_APPLICATION seconds.."
			exit 1
		fi
		sleep 1
	done
	docker-compose logs application
	test "$(docker inspect --format='{{.State.Health.Status}}' mvp_application_1)" = 'healthy' || exit 1
}

function check_rabbitmq_startup () {
	slept=0
	while [[ "$(curl -u guest:guest -sL -w '%{http_code}' http://$TEST_HOST:15672/api/whoami -o /dev/null)" != "200" ]]; do
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
MVP_TAG=$MVP_UPGRADE_FROM_TAG docker-compose up -d postgresql rabbitmq application
echo -n ":: Waiting for MVP application to start up"
check_application_startup

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

MVP_TAG=$MVP_UPGRADE_TO_TAG docker-compose up -d application
echo -n ":: Waiting for new instance of MVP application to start up"
check_application_startup

echo ":: Upgrade successfully completed!"
