#!/bin/sh
# Compila el dibujo del gato fuera de Android y genera hojas de prueba en $1.
set -e
OUT=${1:-/tmp/javi-preview}
SRC=$(dirname "$0")/../../app/src/main/java/com/isebx/javi
HERE=$(dirname "$0")
STD=$(ls /opt/gradle/lib/kotlin-stdlib-[0-9]*.jar)
mkdir -p "$OUT/classes"
java -cp "/opt/gradle/lib/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -cp "$STD" -d "$OUT/classes" \
  "$SRC/Javi.kt" "$SRC/CatLook.kt" "$SRC/Looks.kt" "$SRC/Pen.kt" "$SRC/CatPainter.kt" \
  "$HERE/Stubs.kt" "$HERE/SvgPen.kt" "$HERE/Main.kt" 2>&1 | grep -v "Picked up" || true
java -cp "$OUT/classes:$STD" com.isebx.javi.MainKt "$OUT" 2>&1 | grep -v "Picked up" || true
