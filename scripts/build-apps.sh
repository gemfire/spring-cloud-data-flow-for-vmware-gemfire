#!/bin/bash

#
# Copyright 2025 Broadcom. All rights reserved.
# SPDX-License-Identifier: Apache-2.0
#

set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo "Usage: $0 <image-name> <version> <boot-jar-file>"
  echo "Example: $0 gemfire/gemfire-sink-rabbit 1.0.2 gemfire-sink-rabbit-1.0.2.jar"
  exit 1
fi

IMAGE_NAME="$1"
VERSION="$2"
ARTIFACT="$3"
BUILDER="paketobuildpacks/builder-jammy-base:latest"
PLATFORMS=("linux/amd64" "linux/arm64")

# Build and push each platform-specific image
for PLATFORM in "${PLATFORMS[@]}"; do
  ARCH="${PLATFORM##*/}"
  TAG="${VERSION}-${ARCH}"
  echo "🚀 Building ${IMAGE_NAME}:${TAG} for ${PLATFORM}..."

  pack build "${IMAGE_NAME}:${TAG}" \
    --path "$ARTIFACT" \
    --builder "$BUILDER" \
    --platform "$PLATFORM" \
    --env BP_JVM_VERSION=8 \
    --env BPE_APPEND_JDK_JAVA_OPTIONS=-Dfile.encoding=UTF-8 \
    --env BPE_APPEND_JDK_JAVA_OPTIONS=-Dsun.jnu.encoding \
    --env BPE_LC_ALL=en_US.utf8 \
    --env BPE_LANG=en_US.utf8

  podman push "${IMAGE_NAME}:${TAG}"
done
