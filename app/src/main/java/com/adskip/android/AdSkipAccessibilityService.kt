package com.adskip.android

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 广告跳过无障碍服务
 * 
 * 核心功能：
 * 1. 监听屏幕界面变化
 * 2. 检测包含"跳过"、"关闭"等关键字的按钮
 * 3. 自动执行点击操作
 */
class AdSkipAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AdSkipService"
        
        // 跳过广告的关键字列表
        private val SKIP_KEYWORDS = listOf(
            "跳过", "跳过广告", "关闭",
            "跳過", "關閉",  // 繁体中文
            "点击跳过", "点击关闭",
            "跳过 ", " 跳过",  // 带空格的变体
        )
        
        // 跳过按钮的倒计时关键字（如 "5s 跳过"）
        private val COUNTDOWN_PATTERNS = listOf(
            Regex("""^\d+\s*[sS秒]\s*跳过"""),
            Regex("""^跳过\s*\d+\s*[sS秒]?"""),
            Regex("""^\d+\s*[sS秒]?\s*[Ss]kip"""),
            Regex("""^[Ss]kip\s*(in\s*)?\d+"""),
        )
        
        // 不处理的包名（白名单 - 这些 App 不需要跳过广告）
        private val IGNORED_PACKAGES = setOf(
            "com.android.launcher",
            "com.android.launcher3",
            "com.android.systemui",
            "com.android.settings",
            "com.adskip.android",  // 自己
        )
        
        // 最大节点遍历深度（性能优化）
        private const val MAX_DEPTH = 10
        
        // 点击冷却时间（毫秒）- 避免重复点击
        private const val CLICK_COOLDOWN_MS = 3000L
    }
    
    // 上次点击时间
    private var lastClickTime = 0L
    
    // 上次处理的包名
    private var lastPackageName: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "AdSkip 无障碍服务已启动")
        
        // 动态配置服务
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                   AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val packageName = event.packageName?.toString() ?: return
        
        // 忽略白名单中的包名
        if (packageName in IGNORED_PACKAGES) return
        
        // 忽略系统包名前缀
        if (packageName.startsWith("com.android.") || 
            packageName.startsWith("com.google.android.")) {
            return
        }
        
        // 冷却时间检查
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < CLICK_COOLDOWN_MS) {
            return
        }
        
        // 只处理窗口状态变化和内容变化事件
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                processEvent(packageName)
            }
        }
    }
    
    private fun processEvent(packageName: String) {
        try {
            val rootNode = rootInActiveWindow ?: return
            val activeKeywords = getActiveKeywords(packageName)
            
            // 查找并点击跳过按钮
            val clicked = findAndClickSkipButton(rootNode, 0, activeKeywords)
            
            if (clicked) {
                lastClickTime = System.currentTimeMillis()
                lastPackageName = packageName
                Log.i(TAG, "成功跳过广告: $packageName")
            }
            
            rootNode.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "处理事件时发生错误", e)
        }
    }
    
    /**
     * 递归查找并点击跳过按钮
     * 
     * @param node 当前节点
     * @param depth 当前遍历深度
     * @return 是否成功点击
     */
    private fun findAndClickSkipButton(
        node: AccessibilityNodeInfo?,
        depth: Int,
        activeKeywords: List<String>
    ): Boolean {
        if (node == null || depth > MAX_DEPTH) return false
        
        try {
            // 检查当前节点的文本内容
            val nodeText = node.text?.toString() ?: ""
            val nodeDesc = node.contentDescription?.toString() ?: ""
            val nodeViewId = node.viewIdResourceName ?: ""
            
            // 检查是否匹配跳过关键字
            if (isSkipButton(nodeText, activeKeywords) ||
                isSkipButton(nodeDesc, activeKeywords)) {
                if (tryClickNode(node)) {
                    Log.d(TAG, "点击成功: text='$nodeText', desc='$nodeDesc', id='$nodeViewId'")
                    return true
                }
            }
            
            // 递归遍历子节点
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    if (findAndClickSkipButton(child, depth + 1, activeKeywords)) {
                        child.recycle()
                        return true
                    }
                    child.recycle()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "遍历节点时发生错误", e)
        }
        
        return false
    }
    
    /**
     * 检查文本是否为跳过按钮
     */
    private fun isSkipButton(text: String, activeKeywords: List<String>): Boolean {
        if (text.isBlank()) return false
        
        val trimmedText = text.trim()
        
        // 精确匹配关键字
        if (activeKeywords.any { keyword ->
            trimmedText.equals(keyword, ignoreCase = true) ||
            trimmedText.contains(keyword, ignoreCase = true) && trimmedText.length < 15
        }) {
            return true
        }
        
        // 匹配倒计时模式（如 "5s 跳过"）
        if (COUNTDOWN_PATTERNS.any { pattern -> pattern.containsMatchIn(trimmedText) }) {
            return true
        }
        
        return false
    }

    private fun getActiveKeywords(packageName: String): List<String> {
        val custom = RuleStore(this).getPackageKeywordList(packageName)
        if (custom.isEmpty()) {
            return SKIP_KEYWORDS
        }
        return (SKIP_KEYWORDS + custom).distinct()
    }
    
    /**
     * 尝试点击节点
     */
    private fun tryClickNode(node: AccessibilityNodeInfo): Boolean {
        // 首先尝试直接点击当前节点
        if (node.isClickable) {
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true
            }
        }
        
        // 如果当前节点不可点击，尝试点击父节点
        var parent = node.parent
        var depth = 0
        while (parent != null && depth < 3) {
            if (parent.isClickable) {
                if (parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    parent.recycle()
                    return true
                }
            }
            val oldParent = parent
            parent = parent.parent
            oldParent.recycle()
            depth++
        }
        parent?.recycle()
        
        return false
    }

    override fun onInterrupt() {
        Log.w(TAG, "AdSkip 无障碍服务被中断")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "AdSkip 无障碍服务已停止")
    }
}

