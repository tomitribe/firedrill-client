#!/bin/bash

build() {
	echo "Building Client"
	mvn clean package tomee:exec
}

updateClient() {
	HOSTNAME=${1?Client Host}
	echo "Updating Client on ${HOSTNAME}..."
	scp -i ~/.ssh/tomitribe_dev.pem $PROJECTDIR/target/*-exec.jar admin@$HOSTNAME:~/client-exec.jar

	ssh -i ~/.ssh/tomitribe_dev.pem admin@$HOSTNAME << EOF
sudo supervisorctl stop load
sudo mv ~/client-exec.jar /opt/pidemo/
sudo chown -R load:load /opt/pidemo
sudo supervisorctl reload
sudo supervisorctl status
EOF
	echo "Finished updating client on ${HOSTNAME}, please check the output above for errors"
}

PROJECTDIR=`pwd`
build
for host in ec2-52-43-80-145.us-west-2.compute.amazonaws.com ec2-52-38-166-123.us-west-2.compute.amazonaws.com ec2-52-42-92-217.us-west-2.compute.amazonaws.com ec2-52-41-169-28.us-west-2.compute.amazonaws.com; do
	updateClient $host
done
