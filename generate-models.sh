#!/usr/bin/env bash -ex

# This script fetches the original JSON schema and generates the Kotlin data models using Quicktype
# This avoids any mistake when passing the types from one repo to the next

BUILD_DIR=./build/schema
TARGET_DIR=./library/src/commonMain/kotlin/com/gu/recipe/generated/

FILES=$(curl -s "https://api.github.com/repos/guardian/recipes-backend/contents/schema" | jq -r '.[].download_url')

rm -rf ${BUILD_DIR}
mkdir -p ${BUILD_DIR}
for FILE in ${FILES}; do
  curl "${FILE}" -o "${BUILD_DIR}/$(basename ${FILE})"
done

mkdir -p $TARGET_DIR
npx quicktype \
    --src-lang schema \
    --lang kotlin \
    --framework just-types \
    --package com.gu.recipe.generated \
    --out ${TARGET_DIR}/RecipeModels.kt \
    ${BUILD_DIR}/*.json
