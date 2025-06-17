#!/usr/bin/env sh

# Get absolute directory containing wrapper script
app_dir="$(cd "$(dirname "$0")" && pwd)"

# Set up environment
export JAVA_HOME="C:\Program Files\Java\jdk-24"
export PATH="$JAVA_HOME\bin:$PATH"

# Execute Gradle wrapper
exec "$app_dir/gradle/wrapper/gradle-wrapper.jar" "$@"
