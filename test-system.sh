#!/bin/bash

echo "=== 医疗预约系统功能测试 ==="
echo "测试时间: $(date)"
echo ""

# 测试页面访问
echo "1. 测试页面访问权限:"
pages=("index.html" "doctors.html" "appointment.html" "ai-consultation.html" "profile.html" "appointments.html")
all_pages_ok=true

for page in "${pages[@]}"; do
    status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/$page")
    if [ "$status" = "200" ]; then
        echo "  ✓ $page - OK ($status)"
    else
        echo "  ✗ $page - 失败 ($status)"
        all_pages_ok=false
    fi
done

echo ""

# 测试API接口
echo "2. 测试API接口:"
apis=("api/departments" "api/doctors")
all_apis_ok=true

for api in "${apis[@]}"; do
    status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/$api")
    if [ "$status" = "200" ]; then
        echo "  ✓ $api - OK ($status)"
    else
        echo "  ✗ $api - 失败 ($status)"
        all_apis_ok=false
    fi
done

# 测试需要认证的API
echo ""
echo "3. 测试需要认证的API:"
auth_status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/api/appointments")
if [ "$auth_status" = "403" ]; then
    echo "  ✓ api/appointments - 正确返回403 (需要认证)"
else
    echo "  ✗ api/appointments - 意外状态码: $auth_status"
fi

echo ""

# 测试数据库连接
echo "4. 测试数据库连接:"
db_response=$(curl -s "http://localhost:8080/api/departments")
if [[ "$db_response" == *"id"* && "$db_response" == *"name"* ]]; then
    echo "  ✓ 数据库连接正常 - 能获取到部门数据"
else
    echo "  ✗ 数据库连接可能有问题"
fi

echo ""

# 总结
echo "=== 测试总结 ==="
if [ "$all_pages_ok" = true ] && [ "$all_apis_ok" = true ]; then
    echo "✅ 所有核心功能测试通过！"
    exit 0
else
    echo "❌ 部分测试失败，请检查上述错误"
    exit 1
fi