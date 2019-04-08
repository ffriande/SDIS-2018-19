#!/usr/bin/env bash

javac src/*.java
echo "Compiled code."

# get peerID or use default
PEERID="$1"
PEERID=${PEERID:-"1"}

# get version or use default
VERSION="$2"
VERSION=${VERSION:-"1.0"}

# get access point or use default
ACCESSPOINT="$3"
ACCESSPOINT=${ACCESSPOINT:-"peer1"}

#Usage: <protocolVersion> <peerId> <serviceAccessPoint> <mccIP> <mccPort> <mdbIp> <mdbPort> <mdrIp> <mdrPort>
java -cp ../Projeto1/src Peer $VERSION $PEERID $ACCESSPOINT 224.0.0.1 8001 224.0.0.2 8002 224.0.0.3 8003
