#!/bin/bash
set -euo pipefail

read -p "Command (up / down): " command

if [ "$command" == "up" ]
then
    docker compose -f ./infrastructure/docker/compose.yml up -d
else
    docker compose -f ./infrastructure/docker/compose.yml down
fi

echo "\nCompose $command successfully"
