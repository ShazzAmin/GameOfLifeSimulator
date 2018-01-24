#!/bin/bash
JARNAME="GameOfLifeSimulator"
MAINCLASS="GameOfLifeGUI"

cd "$(dirname "$0")"
javac -d build/ $(find src/ -name "*.java" -type f)
jar cfe bin/$JARNAME.jar $MAINCLASS -C build/ .
find build/ -name "*.class" -type f -delete
find build/ -mindepth 1 -type d -empty -delete
