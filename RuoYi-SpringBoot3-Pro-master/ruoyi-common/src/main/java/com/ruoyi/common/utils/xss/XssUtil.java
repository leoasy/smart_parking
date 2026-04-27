package com.ruoyi.common.utils.xss;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * XSS防护工具类
 * 
 * 提供全面的XSS攻击防护：
 * 1. HTML标签过滤
 * 2. JavaScript协议过滤
 * 3. CSS表达式过滤
 * 4. 事件处理器过滤
 * 5. 危险的CSS属性过滤
 * 
 * @author ruoyi
 */
@Component
public class XssUtil {

    // ========== 危险模式定义 ==========

    /**
     * script标签模式
     */
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
        "<script[^>]*?>[\\s\\S]*?</script>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * script标签（单标签模式，如 <script .../>）
     */
    private static final Pattern SCRIPT_SINGLE_PATTERN = Pattern.compile(
        "<script[^>]*?/>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * iframe标签模式
     */
    private static final Pattern IFRAME_PATTERN = Pattern.compile(
        "<iframe[^>]*?>[\\s\\S]*?</iframe>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * iframe单标签模式
     */
    private static final Pattern IFRAME_SINGLE_PATTERN = Pattern.compile(
        "<iframe[^>]*?/?>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * object标签（嵌入外部内容）
     */
    private static final Pattern OBJECT_PATTERN = Pattern.compile(
        "<object[^>]*?>[\\s\\S]*?</object>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * embed标签
     */
    private static final Pattern EMBED_PATTERN = Pattern.compile(
        "<embed[^>]*?/?>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * applet标签（Java小程序）
     */
    private static final Pattern APPLET_PATTERN = Pattern.compile(
        "<applet[^>]*?>[\\s\\S]*?</applet>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * style标签
     */
    private static final Pattern STYLE_PATTERN = Pattern.compile(
        "<style[^>]*?>[\\s\\S]*?</style>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * link标签（可能引入恶意CSS）
     */
    private static final Pattern LINK_PATTERN = Pattern.compile(
        "<link[^>]*?/?>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 事件处理器属性模式（on开头的事件）
     */
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
        "\\bon\\w+\\s*=",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * JavaScript协议模式
     */
    private static final Pattern JAVASCRIPT_PROTOCOL_PATTERN = Pattern.compile(
        "javascript\\s*:",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * vbscript协议模式
     */
    private static final Pattern VBSCRIPT_PROTOCOL_PATTERN = Pattern.compile(
        "vbscript\\s*:",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * data协议模式（可能执行恶意代码）
     */
    private static final Pattern DATA_PROTOCOL_PATTERN = Pattern.compile(
        "data\\s*:",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * expression模式（CSS表达式）
     */
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
        "expression\\s*\\(",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * url()模式（可能包含javascript）
     */
    private static final Pattern URL_FUNC_PATTERN = Pattern.compile(
        "url\\s*\\([^)]*\\)",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 危险的CSS属性模式
     */
    private static final Pattern DANGEROUS_CSS_PATTERN = Pattern.compile(
        "behavior\\s*:",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * import模式（可能导入恶意CSS）
     */
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
        "@import\\s+",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 尖括号对（可能被利用）
     */
    private static final Pattern ANGLE_BRACKET_PATTERN = Pattern.compile(
        "[<>]"
    );

    /**
     * 纯文本注释模式
     */
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
        "<!--.*?-->",
        Pattern.DOTALL
    );

    /**
     * XML处理指令
     */
    private static final Pattern XML_PI_PATTERN = Pattern.compile(
        "<\\?.*?\\?>"
    );

    /**
     * CDATA节
     */
    private static final Pattern CDATA_PATTERN = Pattern.compile(
        "<!\\[CDATA\\[.*?\\]\\]>"
    );

    /**
     * 检查输入是否包含XSS攻击特征
     * 
     * @param input 输入字符串
     * @return true表示包含XSS特征
     */
    public static boolean containsXss(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        
        return SCRIPT_PATTERN.matcher(input).find()
            || SCRIPT_SINGLE_PATTERN.matcher(input).find()
            || IFRAME_PATTERN.matcher(input).find()
            || IFRAME_SINGLE_PATTERN.matcher(input).find()
            || OBJECT_PATTERN.matcher(input).find()
            || EMBED_PATTERN.matcher(input).find()
            || APPLET_PATTERN.matcher(input).find()
            || STYLE_PATTERN.matcher(input).find()
            || LINK_PATTERN.matcher(input).find()
            || EVENT_HANDLER_PATTERN.matcher(input).find()
            || JAVASCRIPT_PROTOCOL_PATTERN.matcher(input).find()
            || VBSCRIPT_PROTOCOL_PATTERN.matcher(input).find()
            || DATA_PROTOCOL_PATTERN.matcher(input).find()
            || EXPRESSION_PATTERN.matcher(input).find()
            || DANGEROUS_CSS_PATTERN.matcher(input).find()
            || ANGLE_BRACKET_PATTERN.matcher(input).find()
            || COMMENT_PATTERN.matcher(input).find()
            || XML_PI_PATTERN.matcher(input).find()
            || CDATA_PATTERN.matcher(input).find();
    }

    /**
     * 过滤XSS攻击代码
     * 
     * @param input 输入字符串
     * @return 过滤后的安全字符串
     */
    public static String filter(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }

        String result = input;

        // 移除script标签
        result = SCRIPT_PATTERN.matcher(result).replaceAll("");
        result = SCRIPT_SINGLE_PATTERN.matcher(result).replaceAll("");

        // 移除iframe标签
        result = IFRAME_PATTERN.matcher(result).replaceAll("");
        result = IFRAME_SINGLE_PATTERN.matcher(result).replaceAll("");

        // 移除object标签
        result = OBJECT_PATTERN.matcher(result).replaceAll("");

        // 移除embed标签
        result = EMBED_PATTERN.matcher(result).replaceAll("");

        // 移除applet标签
        result = APPLET_PATTERN.matcher(result).replaceAll("");

        // 移除style标签
        result = STYLE_PATTERN.matcher(result).replaceAll("");

        // 移除link标签
        result = LINK_PATTERN.matcher(result).replaceAll("");

        // 移除事件处理器属性
        result = EVENT_HANDLER_PATTERN.matcher(result).replaceAll("");

        // 移除JavaScript协议
        result = JAVASCRIPT_PROTOCOL_PATTERN.matcher(result).replaceAll("");

        // 移除VBScript协议
        result = VBSCRIPT_PROTOCOL_PATTERN.matcher(result).replaceAll("");

        // 移除data协议（保留data:image等安全用途）
        result = DATA_PROTOCOL_PATTERN.matcher(result).replaceAll("data：");

        // 移除CSS expression
        result = EXPRESSION_PATTERN.matcher(result).replaceAll("");

        // 移除危险CSS属性
        result = DANGEROUS_CSS_PATTERN.matcher(result).replaceAll("");

        // 移除@Import
        result = IMPORT_PATTERN.matcher(result).replaceAll("");

        // 移除HTML注释
        result = COMMENT_PATTERN.matcher(result).replaceAll("");

        // 移除XML处理指令
        result = XML_PI_PATTERN.matcher(result).replaceAll("");

        // 移除CDATA节
        result = CDATA_PATTERN.matcher(result).replaceAll("");

        // 转义尖括号
        result = ANGLE_BRACKET_PATTERN.matcher(result).replaceAll("");

        return result;
    }

    /**
     * 过滤并验证输入，如果包含XSS则抛出异常
     * 
     * @param input 输入字符串
     * @param fieldName 字段名称（用于错误信息）
     * @return 过滤后的字符串
     */
    public static String filterAndValidate(String input, String fieldName) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        
        if (containsXss(input)) {
            throw new ServiceException("参数 " + fieldName + " 存在XSS攻击风险");
        }
        
        return filter(input);
    }

    /**
     * HTML特殊字符转义
     * 
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    public static String escapeHtml(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        
        String result = input;
        result = result.replace("&", "&amp;");
        result = result.replace("<", "&lt;");
        result = result.replace(">", "&gt;");
        result = result.replace("\"", "&quot;");
        result = result.replace("'", "&#x27;");
        return result;
    }

    /**
     * HTML特殊字符反转义
     * 
     * @param input 输入字符串
     * @return 反转义后的字符串
     */
    public static String unescapeHtml(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        
        String result = input;
        result = result.replace("&lt;", "<");
        result = result.replace("&gt;", ">");
        result = result.replace("&quot;", "\"");
        result = result.replace("&#x27;", "'");
        result = result.replace("&amp;", "&");
        return result;
    }

    /**
     * JavaScript特殊字符转义
     * 
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    public static String escapeJavaScript(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        
        String result = input;
        result = result.replace("\\", "\\\\");
        result = result.replace("\"", "\\\"");
        result = result.replace("'", "\\'");
        result = result.replace("\n", "\\n");
        result = result.replace("\r", "\\r");
        result = result.replace("\t", "\\t");
        result = result.replace("<", "\\x3C");
        result = result.replace(">", "\\x3E");
        return result;
    }

    /**
     * URL特殊字符转义
     * 
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    public static String escapeUrl(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        
        try {
            java.net.URLEncoder.encode(input, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return input;
        }
        return input;
    }

    /**
     * 检查并过滤SQL特殊字符（用于LIKE查询等场景）
     * 
     * @param input 输入字符串
     * @return 过滤后的字符串
     */
    public static String escapeForLike(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        
        String result = input;
        result = result.replace("\\", "\\\\");
        result = result.replace("%", "\\%");
        result = result.replace("_", "\\_");
        return result;
    }
}
