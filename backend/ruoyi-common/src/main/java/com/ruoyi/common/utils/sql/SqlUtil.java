package com.ruoyi.common.utils.sql;

import com.ruoyi.common.exception.UtilException;
import com.ruoyi.common.utils.StringUtils;

/**
 * sql操作工具类
 * 
 * 安全加固版本：
 * 1. 增强SQL注入关键字检测
 * 2. 添加更多危险模式识别
 * 3. 改进正则表达式性能
 * 
 */
public class SqlUtil
{
    /**
     * 定义常用的 sql关键字 (增强版)
     * 包含：SQL注入常用关键字、函数、注释等
     */
    public static String SQL_REGEX = 
        "\u000B|" +                           // 垂直制表符（恶意注入常用）
        "and |" +
        "extractvalue|" +
        "updatexml|" +
        "sleep\\(|" +                         // sleep()函数
        "benchmark\\(|" +                     // benchmark()函数
        "information_schema|" +
        "performance_schema|" +
        "mysql\\.|" +                         // MySQL系统库
        "pg_catalog|" +                       // PostgreSQL系统库
        "exec\\s*\\(|" +                      // exec(
        "execute\\s*\\(|" +                   // execute(
        "insert\\s+into|" +
        "select\\s+from|" +
        "delete\\s+from|" +
        "update\\s+.*\\s+set|" +
        "drop\\s+(table|database|index)|" +
        "truncate\\s+|" +
        "alter\\s+table|" +
        "create\\s+(table|database|index|procedure)|" +
        "grant\\s+|" +                        // 权限赋予
        "revoke\\s+|" +                       // 权限撤销
        "deny\\s+|" +                         // 拒绝权限
        "union\\s+(all\\s+)?select|" +
        "union\\s+select|" +
        "load_file\\(|" +                     // 读取文件
        "into\\s+(out|dump)file|" +           // 写入文件
        "hex\\(|" +                           // 编码函数
        "unhex\\(|" +                         
        "char\\(|" +
        "chr\\(|" +
        "mid\\(|" +
        "substring\\(|" +
        "concat\\(|" +                        // 字符串拼接
        "concat_ws\\(|" +
        "group_concat\\(|" +
        "make_set\\(|" +
        "ascii\\(|" +
        "ord\\(|" +
        "|||" +                              // 字符串连接符
        "master\\.|" +
        "xp_cmdshell|" +                     // SQL Server危险存储过程
        "sp_executesql|" +                   // SQL Server危险存储过程
        "openrowset|" +                      // SQL Server链接服务器
        "opendatasource|" +
        "like\\s+'%|" +                      // LIKE注入
        "like\\s+[^a-zA-Z]|" +               // 非法的LIKE模式
        "order\\s+by\\s+[^0-9]|" +           // 非法的ORDER BY
        "--|" +                               // SQL注释
        "/\\*|" +                             // 注释开始
        "\\*/|" +                             // 注释结束
        ";--|" +                              // 多语句注释
        "\\+\\s*\\+|" +                      // 递增操作
        "@@|" +                               // 系统变量
        "user\\(|" +                          // user()函数
        "version\\(|" +                       // version()函数
        "database\\(|" +                      // database()函数
        "current_user|" +
        "session_user|" +
        "system_user|" +
        "rand\\(|" +                          // rand()函数
        "count\\(|" +
        "length\\(|" +
        "lengthb\\(|" +
        "substr\\(|" +
        "substrb\\(|" +
        "substring\\(|" +
        "substring_index\\(|" +
        "floor\\(|" +                        // floor()常用于报错注入
        "extractvalue\\(|" +                 // XPath报错注入
        "updatexml\\(|" +                     // XPath报错注入
        "exp\\(|" +                           // 指数函数（某些注入利用）
        "NamePipe|" +                         //命名管道
        "\\\\\\\\";

    /**
     * 仅支持字母、数字、下划线、空格、逗号、小数点（支持多个字段排序）
     */
    public static String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";

    /**
     * 限制orderBy最大长度
     */
    private static final int ORDER_BY_MAX_LENGTH = 500;

    /**
     * 危险SQL模式（用于额外检测）
     */
    private static final String DANGEROUS_PATTERNS[] = {
        ".*\\bor\\b.*=\\b.*",                // or 1=1 类型注入
        ".*\\band\\b.*=\\b.*",                // and 1=1 类型注入
        ".*'\\s*or\\s*'1'\\s*=\\s*'1",       // 'or'1'='1
        ".*'\\s*or\\s*1\\s*=\\s*1",           // 'or 1=1
        ".*\"\\s*or\\s*\"1\"\\s*=\\s*\"1",    // "or"1"="1
        ".*\\bor\\b.*\\b1\\b=\\b1\\b",        // or 1=1
        ".*--\\s*$",                          // 行内注释
        ".*;\\s*drop\\b",                     // ;drop
        ".*;\\s*delete\\b",                   // ;delete
        ".*;\\s*update\\b",                   // ;update
        ".*;\\s*insert\\b",                   // ;insert
        ".*exec\\s+master",                  // exec master
        ".*\\bxp_cmdshell\\b",               // xp_cmdshell
        ".*\\bsp_executesql\\b",             // sp_executesql
        ".*into\\s+(out|dump)file",          // into out/dumpfile
        ".*load_file\\s*\\(",                // load_file(
        ".*\\bconcat\\s*\\(",                // concat(
        ".*\\bchar\\s*\\(",                  // char(
    };

    /**
     * 检查字符，防止注入绕过
     * 
     * @param value 输入值
     * @return 过滤后的安全值
     */
    public static String escapeOrderBySql(String value)
    {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value))
        {
            throw new UtilException("参数不符合规范，不能进行查询");
        }
        if (StringUtils.length(value) > ORDER_BY_MAX_LENGTH)
        {
            throw new UtilException("参数已超过最大限制，不能进行查询");
        }
        return value;
    }

    /**
     * 验证 order by 语法是否符合规范
     */
    public static boolean isValidOrderBySql(String value)
    {
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        // 先检查基本模式
        if (!value.matches(SQL_PATTERN)) {
            return false;
        }
        // 再检查危险模式
        return !containsDangerousPattern(value);
    }

    /**
     * 检查是否包含危险SQL模式
     * 
     * @param value 输入值
     * @return true表示包含危险模式
     */
    private static boolean containsDangerousPattern(String value)
    {
        String lowerValue = value.toLowerCase();
        for (String pattern : DANGEROUS_PATTERNS) {
            if (lowerValue.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * SQL关键字检查
     * 
     * @param value 输入值
     */
    public static void filterKeyword(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return;
        }
        
        // 先进行基本SQL关键字检查
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords)
        {
            if (StringUtils.isNotEmpty(sqlKeyword) && StringUtils.indexOfIgnoreCase(value, sqlKeyword.trim()) > -1)
            {
                throw new UtilException("参数存在SQL注入风险");
            }
        }
        
        // 再进行危险模式检查
        if (containsDangerousPattern(value)) {
            throw new UtilException("参数存在SQL注入风险");
        }
    }

    /**
     * 检验输入是否为安全的外键值（只允许数字和简单字母）
     * 
     * @param value 输入值
     * @return true表示安全
     */
    public static boolean isSafeForeignKeyValue(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return true;
        }
        return value.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * 检验输入是否为安全的标识符（表名、列名等）
     * 
     * @param value 输入值
     * @return true表示安全
     */
    public static boolean isSafeIdentifier(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return false;
        }
        // 标识符只允许字母、数字、下划线，且不能以数字开头
        return value.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
}
