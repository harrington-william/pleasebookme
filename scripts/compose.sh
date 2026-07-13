#!/bin/bash
set -euo pipefail

read command

if [ "$command" == "up" ]
then
    docker compose -f ./infrastructure/docker/compose.yml up -d
else
    docker compose -f ./infrastructure/docker/compose.yml down
fi
