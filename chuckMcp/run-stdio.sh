#!/bin/zsh

set -eu

JAVA_HOME_DIR="${1:-/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home}"
SCRIPT_DIR="${0:A:h}"
CLASSES_DIR="$SCRIPT_DIR/target/classes"
DEPENDENCY_DIR="$SCRIPT_DIR/target/dependency"

if [[ ! -d "$CLASSES_DIR" ]]; then
  print -u2 "Missing compiled classes in $CLASSES_DIR"
  exit 1
fi

if [[ ! -d "$DEPENDENCY_DIR" ]]; then
  print -u2 "Missing runtime dependencies in $DEPENDENCY_DIR"
  print -u2 "Run: export JAVA_HOME=$JAVA_HOME_DIR && mvn -pl chuckMcp -DskipTests compile dependency:copy-dependencies -DincludeScope=runtime"
  exit 1
fi

classpath="$CLASSES_DIR"
for jar in "$DEPENDENCY_DIR"/*.jar; do
  classpath="$classpath:$jar"
done

export JAVA_HOME="$JAVA_HOME_DIR"
exec "$JAVA_HOME/bin/java" -cp "$classpath" com.vco.chuckmcp.ChuckMcpApplication
