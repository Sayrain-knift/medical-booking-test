#!/bin/bash

echo "=== 简化预约接口流控测试 ==="
echo

# 1. 查看预约相关的流控规则
echo "1. 查看预约接口流控规则："
curl -s "http://localhost:8719/getRules?type=flow" | jq '.[] | select(.resource | contains("Appointment")) | {resource: .resource, count: .count}'
echo

# 2. 快速测试获取预约列表（50次/秒限制）
echo "2. 快速测试获取预约列表接口（50次/秒限制）："
echo "发送60次快速请求..."

success_count=0
blocked_count=0

for i in {1..60}; do
  response=$(curl -s -w "%{http_code}" -o /tmp/response_$i.json "http://localhost:8080/api/appointments")
  http_code=$(echo "$response" | tail -c 3)
  
  if [ "$http_code" = "200" ]; then
    ((success_count++))
    if [ $((i % 10)) -eq 0 ]; then
      echo "第$i次: 正常(HTTP 200)"
    fi
  elif [ "$http_code" = "429" ]; then
    ((blocked_count++))
    echo "第$i次: 流控触发(HTTP 429)"
  else
    echo "第$i次: 其他状态码(HTTP $http_code)"
  fi
done

echo "获取预约列表测试完成：成功 $success_count 次，流控 $blocked_count 次"
echo

# 3. 清理临时文件
rm -f /tmp/response_*.json

echo "=== 测试完成 ==="