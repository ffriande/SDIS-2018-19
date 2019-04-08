#!/usr/bin/env bash

# get filename or use default
FILENAME="$1"
FILENAME=${FILENAME:-"test1.png"}

# get peerID or use default
PEERID="$2"
PEERID=${PEERID:-"1"}

#get the action
ACTION="$3"

#get the optional replicationDegree or spaceReclaim
OPT="$4"
OPT=${OPT:-"2"}

if [ $ACTION = "BACKUP" ]; then
    java TestApp $PEERID BACKUP $FILENAME $OPT
elif [ $ACTION = "RESTORE" ]; then
    java TestApp $PEERID RESTORE $FILENAME
elif [ $ACTION = "DELETE" ]; then
    java TestApp $PEERID DELETE $FILENAME
elif [ $ACTION = "RECLAIM" ]; then
    java TestApp $PEERID RECLAIM $OPT
elif [ $ACTION = "STATE" ]; then
    java TestApp $PEERID STATE
else
    echo Invalid action $ACTION must be RECLAIM, STATE, BACKUP, RESTORE, DELETE.
fi

#Usage: <fileName:default=test1.png> <peerId:default=1> <action> <replicationDegree|spaceReclaim:default=2>