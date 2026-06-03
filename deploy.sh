#!/bin/bash
set -e
echo "🚀 开始部署项目"
docker compose -f compose.prod.yaml up -d --build
sleep 10
docker compose ps
echo "✅ 部署完成"