#!/usr/bin/env bash

set -fx
JDIR=$(dirname $0)/../
exec java  -classpath "$JDIR/target/*:$JDIR/target/lib/*"  ${EXECMAIN:=ptrman.visualizationTests.VisualizeLinesegmentsAnnealing} "$@"
