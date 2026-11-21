#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

find_tool() {
    local tool="$1"

    if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/$tool" ]]; then
        printf '%s\n' "$JAVA_HOME/bin/$tool"
        return
    fi

    if command -v "$tool" >/dev/null 2>&1; then
        command -v "$tool"
        return
    fi

    if [[ -d "$HOME/.jdks" ]]; then
        find "$HOME/.jdks" -maxdepth 3 -type f -name "$tool" -print -quit
        return
    fi

    return 1
}

JAVA_BIN="$(find_tool java || true)"
JAVAC_BIN="$(find_tool javac || true)"

if [[ -z "$JAVA_BIN" || -z "$JAVAC_BIN" ]]; then
    echo "Не найдены java/javac. Установите JDK 17+ или задайте JAVA_HOME." >&2
    exit 1
fi

CP="lib/jfreechart-1.5.5.jar"
mkdir -p out

mapfile -t SOURCES < <(find src/main/java -name '*.java' | sort)
"$JAVAC_BIN" --release 17 -encoding UTF-8 -cp "$CP" -d out "${SOURCES[@]}"
"$JAVA_BIN" -cp "out:$CP" championship.Main "$@"
