#!/bin/bash

echo "=========================================="
echo "🎯 Sentinel流控功能核心演示"
echo "=========================================="

echo ""
echo "📊 当前Sentinel流控规则："
curl -s "http://localhost:8719/getRules?type=flow" | jq -r '.[] | "\(.resource): \(.count)次/秒"'

echo ""
echo "🧪 演示1: 登录接口流控 (10次/秒限制)"
echo "发送12个连续登录请求..."

for i in {1..12}; do
    response=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"wrong","password":"wrong"}')
    
    message=$(echo "$response" | jq -r '.message')
    
    if [[ "$message" == "Invalid username or password" ]]; then
        echo "请求 $i: ✅ 正常处理"
    else
        echo "请求 $i: 🚫 流控触发 - $message"
    fi
done

echo ""
echo "🔥 演示完成！"
echo "可以看到前10个请求正常处理，第11-12个请求被流控拦截"
echo "=========================================="