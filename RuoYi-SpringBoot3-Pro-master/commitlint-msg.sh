#!/bin/sh
# commitlint 提交信息规范检查

# 获取提交消息文件 (commitlint在traditional commitmsg hook中会传递$1)
COMMIT_MSG_FILE=$1
COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")

# commitlint 配置规则:
# feat: 新功能
# fix: 修复bug
# docs: 文档变更
# style: 代码格式（不影响功能）
# refactor: 重构（不是修复也不是新功能）
# perf: 性能优化
# test: 测试
# build: 构建工具或依赖变更
# ci: CI配置变更
# chore: 其他变更
# revert: 回滚

# 定义允许的类型
ALLOWED_TYPES="feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert"

# 检查提交信息是否符合规范
if ! echo "$COMMIT_MSG" | grep -qE "^[a-zA-Z0-9_-]+($ALLOWED_TYPES)" | head -1; then
    echo "提交信息格式不正确!"
    echo "正确格式: <type>(<scope>): <subject>"
    echo "例如: feat(parking): add new detection endpoint"
    echo ""
    echo "允许的类型: feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert"
    exit 1
fi

exit 0