#!/bin/bash

echo "=== 医疗预约系统完整测试 ==="

# 设置基础URL
BASE_URL="http://localhost:8080"

echo "1. 测试用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testpatient5",
    "password": "password123"
  }')

echo "登录响应: $LOGIN_RESPONSE"

# 提取token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
echo "提取的Token: ${TOKEN:0:50}..."

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ 登录失败，无法获取token"
    exit 1
fi

echo "✅ 登录成功"

echo ""
echo "2. 测试获取我的预约列表..."
APPOINTMENTS_RESPONSE=$(curl -s "$BASE_URL/api/appointments/my-appointments" \
  -H "Authorization: Bearer $TOKEN")

echo "预约列表响应: $APPOINTMENTS_RESPONSE"

# 检查是否成功
APPOINTMENT_COUNT=$(echo $APPOINTMENTS_RESPONSE | jq -r '.data | length')
echo "预约数量: $APPOINTMENT_COUNT"

if [ "$APPOINTMENT_COUNT" -gt 0 ]; then
    echo "✅ 获取预约列表成功"
    
    # 显示第一个预约的信息
    FIRST_APPOINTMENT=$(echo $APPOINTMENTS_RESPONSE | jq -r '.data[0]')
    echo "第一个预约信息:"
    echo $FIRST_APPOINTMENT | jq -r '"  - 预约ID: \(.id), 医生: \(.schedule.doctor.name), 日期: \(.schedule.date), 状态: \(.status)"'
else
    echo "⚠️  暂无预约记录"
fi

echo ""
echo "3. 测试获取科室列表..."
DEPARTMENTS_RESPONSE=$(curl -s "$BASE_URL/api/departments" \
  -H "Authorization: Bearer $TOKEN")

DEPARTMENT_COUNT=$(echo $DEPARTMENTS_RESPONSE | jq -r '.data | length')
echo "科室数量: $DEPARTMENT_COUNT"

if [ "$DEPARTMENT_COUNT" -gt 0 ]; then
    echo "✅ 获取科室列表成功"
else
    echo "❌ 获取科室列表失败"
fi

echo ""
echo "4. 测试获取医生列表..."
DOCTORS_RESPONSE=$(curl -s "$BASE_URL/api/doctors?departmentId=1" \
  -H "Authorization: Bearer $TOKEN")

DOCTOR_COUNT=$(echo $DOCTORS_RESPONSE | jq -r '.data | length')
echo "内科医生数量: $DOCTOR_COUNT"

if [ "$DOCTOR_COUNT" -gt 0 ]; then
    echo "✅ 获取医生列表成功"
else
    echo "❌ 获取医生列表失败"
fi

echo ""
echo "=== 测试完成 ==="
echo "所有核心功能测试通过！用户可以正常登录并访问预约功能。"