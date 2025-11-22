#!/bin/bash

echo "=== 预约接口流控功能演示 ==="
echo

# 1. 查看预约相关的流控规则
echo "1. 查看预约接口流控规则："
curl -s "http://localhost:8719/api/rules" | jq '.[] | select(.resource | contains("Appointment") or contains("appointment")) | {resource: .resource, grade: .grade, count: .count, strategy: .strategy}'
echo

# 2. 创建测试用户并获取token
echo "2. 创建测试用户并获取token："
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser'$(date +%s)'",
    "password": "test123",
    "email": "test@example.com",
    "role": "PATIENT"
  }' | jq -r '.token // empty')

if [ -z "$TOKEN" ]; then
  echo "注册失败，尝试使用现有用户登录..."
  TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
      "username": "testuser",
      "password": "test123"
    }' | jq -r '.token // empty')
fi

if [ -z "$TOKEN" ]; then
  echo "无法获取token，跳过需要认证的测试"
  exit 1
fi

echo "获取到token: ${TOKEN:0:20}..."
echo

# 3. 测试获取预约列表接口（限制50次/秒）
echo "3. 测试获取预约列表接口（限制50次/秒）："
echo "发送55次请求，前50次应该正常，后5次应该被流控..."

success_count=0
blocked_count=0

for i in {1..55}; do
  response=$(curl -s -H "Authorization: Bearer $TOKEN" \
    "http://localhost:8080/api/appointments?page=1&pageSize=10")
  
  if echo "$response" | grep -q "请求过于频繁"; then
    echo "第$i次: 流控触发"
    ((blocked_count++))
  else
    echo "第$i次: 正常响应"
    ((success_count++))
  fi
  
  # 不加延迟，快速发送请求
done

echo "获取预约列表测试完成：成功 $success_count 次，流控 $blocked_count 次"
echo

# 4. 测试创建预约接口（限制20次/秒）
echo "4. 测试创建预约接口（限制20次/秒）："
echo "发送25次请求，前20次应该正常，后5次应该被流控..."

success_count=0
blocked_count=0

for i in {1..25}; do
  response=$(curl -s -X POST -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "doctorId": 1,
      "scheduleId": 1,
      "appointmentTime": "2024-12-25T10:00:00",
      "reason": "常规检查"
    }' \
    "http://localhost:8080/api/appointments")
  
  if echo "$response" | grep -q "请求过于频繁"; then
    echo "第$i次: 流控触发"
    ((blocked_count++))
  else
    echo "第$i次: 正常响应"
    ((success_count++))
  fi
  
  # 不加延迟，快速发送请求
done

echo "创建预约测试完成：成功 $success_count 次，流控 $blocked_count 次"
echo

# 5. 查看实时流控统计
echo "5. 查看实时流控统计："
curl -s "http://localhost:8719/api/queryResource" | jq '.[] | select(.resource | contains("Appointment") or contains("appointment")) | {resource: .resource, passQps: .passQps, blockQps: .blockQps, totalQps: .totalQps}'
echo

echo "=== 预约接口流控功能演示完成 ==="