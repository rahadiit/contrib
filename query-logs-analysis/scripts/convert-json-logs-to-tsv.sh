#!/usr/bin/env bash

source ./scripts/config.sh

EXPECTED_ARGS=2
E_BADARGS=65

source scripts/config.sh

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` europeana-log-file.json europeana-log-file.tsv"
  exit $E_BADARGS
fi

echo "convert log in $1"

$JAVA eu.europeana.querylog.cli.EuropeanaJsonLogsToTsvCLI -input $1 -output $2

echo "log converted in $2"



