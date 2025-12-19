#!/bin/bash

# Script para limpiar y compilar el proyecto CodeFM
# Genera los DTOs desde OpenAPI y compila todos los módulos

echo "========================================="
echo "Limpiando y compilando proyecto CodeFM"
echo "========================================="

echo ""
echo "1. Limpiando proyecto..."
./mvnw clean

echo ""
echo "2. Compilando módulos..."
./mvnw compile

echo ""
echo "3. Verificando compilación exitosa..."
if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa"
    echo ""
    echo "Archivos generados:"
    echo "  - DTOs en: codefm-api/target/generated-sources/"
    echo "  - Mappers en: target/generated-sources/annotations/"
else
    echo "❌ Error en la compilación"
    exit 1
fi

echo ""
echo "4. Ejecutando tests..."
./mvnw test

if [ $? -eq 0 ]; then
    echo "✅ Tests ejecutados correctamente"
else
    echo "⚠️ Algunos tests fallaron"
fi

echo ""
echo "========================================="
echo "Proceso completado"
echo "========================================="

