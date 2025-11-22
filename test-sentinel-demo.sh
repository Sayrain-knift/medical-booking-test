#!/bin/bash

echo "=========================================="
echo "🚀 Sentinel流控功能演示测试"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 基础URL
BASE_URL="http://localhost:8080"

echo -e "${BLUE}📋 测试计划：${NC}"
echo "1. 注册接口流控测试 (5次/秒)"
echo "2. 登录接口流控测试 (10次/秒)" 
echo "3. 查看当前Sentinel规则"
echo "4. 实时监控数据"
echo ""

# 检查应用是否运行
echo -e "${BLUE}🔍 检查应用状态...${NC}"
if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ 应用运行正常${NC}"
else
    echo -e "${RED}❌ 应用未运行，请先启动应用${NC}"
    exit 1
fi

# 1. 查看当前Sentinel规则
echo -e "${BLUE}📊 当前Sentinel流控规则：${NC}"
curl -s "http://localhost:8719/getRules?type=flow" | jq -r '.[] | "\(.resource): \(.count)次/秒"'
echo ""

# 2. 注册接口流控测试 (5次/秒)
echo -e "${YELLOW}🧪 测试1: 注册接口流控 (限制5次/秒)${NC}"
echo "发送8个连续注册请求..."
for i in {1..8}; do
    response=$(curl -s -X POST "$BASE_URL/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"testflow${i}$(date +%s)\",\"password\":\"password123\",\"email\":\"test${i}@test.com\",\"phone\":\"1380013800${i}\",\"role\":\"PATIENT\"}")
    
    message=$(echo "$response" | jq -r '.message // "未知响应"')
    
    if [[ "$message" == "User registered successfully!" ]]; then
        echo -e "请求 $i: ${GREEN}$message${NC}"
    elif [[ "$message" == *"频繁"* ]]; then
        echo -e "请求 $i: ${RED}🚫 流控触发: $message${NC}"
    else
        echo -e "请求 $i: ${YELLOW}$message${NC}"
    fi
done
echo ""

# 等待1秒让规则重置
sleep 1

# 3. 登录接口流控测试 (10次/秒)
echo -e "${YELLOW}🧪 测试2: 登录接口流控 (限制10次/秒)${NC}"
echo "发送15个连续登录请求..."
for i in {1..15}; do
    response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"wrong","password":"wrong"}')
    
    message=$(echo "$response" | jq -r '.message // "未知响应"')
    
    if [[ "$message" == "Invalid username or password" ]]; then
        echo -e "请求 $i: ${GREEN}正常处理${NC}"
    elif [[ "$message" == *"频繁"* ]]; then
        echo -e "请求 $i: ${RED}🚫 流控触发: $message${NC}"
    else
        echo -e "请求 $i: ${YELLOW}$message${NC}"
    fi
done
echo ""

# 4. 获取一个有效token测试其他接口
echo -e "${BLUE}🔑 获取测试token...${NC}"
token_response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"testpatient","password":"password123"}')

token=$(echo "$token_response" | jq -r '.token // empty')

if [[ -n "$token" && "$token" != "null" ]]; then
    echo -e "${GREEN}✅ Token获取成功${NC}"
    
    # 5. 测试预约接口熔断
    echo -e "${YELLOW}🧪 测试3: 预约接口熔断降级${NC}"
    echo "发送预约查询请求..."
    
    for i in {1..3}; do
        response=$(curl -s -X GET "$BASE_URL/api/appointments" \
            -H "Authorization: Bearer $token")
        
        message=$(echo "$response" | jq -r '.message // "正常响应"')
        
        if [[ "$message" == *"服务暂时不可用"* ]]; then
            echo -e "请求 $i: ${RED}🔥 熔断降级: $message${NC}"
        elif [[ "$message" == *"成功"* ]]; then
            echo -e "请求 $i: ${GREEN}正常响应${NC}"
        else
            echo -e "请求 $i: ${YELLOW}$message${NC}"
        fi
    done
else
    echo -e "${RED}❌ Token获取失败，跳过需要认证的测试${NC}"
fi

echo ""

# 6. 显示实时统计
echo -e "${BLUE}📈 Sentinel实时统计信息：${NC}"
echo "当前活跃资源:"
curl -s "http://localhost:8719/api/queryResource" 2>/dev/null || echo "无法获取资源信息"

echo ""
echo "=========================================="
echo -e "${GREEN}🎉 Sentinel流控功能演示完成！${NC}"
echo "=========================================="