#!/usr/bin/env bash

clear
mkdir bin

javac -cp .:./bin/:./ -d bin src/*/*.java
echo "Source code compiled"

# get peerID or use default
PEERID="$1"
PEERID=${PEERID:-"1"}  # If variable not set, use default.

# if the second argument equals 1 then delete the database
if [ "$2" = "1" ]
then
    echo Deleting internal_state_peer_$PEERID.
    rm -rf internal_state_peer_$PEERID
fi


if [ "$3" = "1" ]
then
    cd bin
    gnome-terminal --geometry 20x20+1000+800 -e rmiregistry
    cd ..
fi

# get peerID or use default
VERSION="$4"
VERSION=${VERSION:-"1.0"}  # If variable not set, use default.

#Usage: <protocolVersion> <peerId> <serviceAccessPoint> <mccIP> <mccPort> <mdbIp> <mdbPort> <mdrIp> <mdrPort>
java -cp .:./bin/ src.main.Peer $VERSION $PEERID 8499 224.0.0.0 9000 224.0.0.1 9001 224.0.0.2 9002

# Usage: <peerId:default=1> <resetDatabase:default=false> <startRMI:default=false>
