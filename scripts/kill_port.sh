#!/bin/bash
set -euo pipefail

lsof -ti:8080 | xargs kill -9; ./gradlew bootRun