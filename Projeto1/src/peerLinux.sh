#!/usr/bin/env bash

javac *.java
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
java Peer $VERSION $PEERID $ACCESSPOINT 224.0.0.88 8001 224.0.0.89 8002 224.0.0.90 8003