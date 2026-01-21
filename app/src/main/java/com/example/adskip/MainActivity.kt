package com.example.adskip

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import com.example.adskip.databinding.ActivityMainBinding

/**
 * 主界面
 * 
 * 功能：
 * 1. 显示无障碍服务状态
 * 2. 提供跳转到系统设置的入口
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        syncInstalledApps()
        setupUI()
    }
    
    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }
    
    private fun setupUI() {
        // 跳转到无障碍设置
        binding.btnOpenSettings.setOnClickListener {
            openAccessibilitySettings()
        }
        
        // 刷新状态
        binding.btnRefresh.setOnClickListener {
            updateServiceStatus()
        }

        binding.btnRules.setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }
    }
    
    private fun updateServiceStatus() {
        val isEnabled = isAccessibilityServiceEnabled()
        
        if (isEnabled) {
            binding.tvStatus.text = "服务状态：已启用 ✓"
            binding.tvStatus.setTextColor(getColor(R.color.status_enabled))
            binding.statusIndicator.setBackgroundResource(R.drawable.status_indicator_enabled)
            binding.tvTip.text = "广告跳过功能已激活，现在可以自动跳过开屏广告了！"
        } else {
            binding.tvStatus.text = "服务状态：未启用"
            binding.tvStatus.setTextColor(getColor(R.color.status_disabled))
            binding.statusIndicator.setBackgroundResource(R.drawable.status_indicator_disabled)
            binding.tvTip.text = "请点击下方按钮，在系统设置中启用无障碍服务"
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        )
        
        return enabledServices.any { serviceInfo ->
            serviceInfo.resolveInfo.serviceInfo.packageName == packageName &&
            serviceInfo.resolveInfo.serviceInfo.name == AdSkipAccessibilityService::class.java.name
        }
    }
    
    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            // 如果直接跳转失败，尝试通用设置
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }

    private fun syncInstalledApps() {
        val packages = AppScanner.scanLauncherPackages(this)
        RuleStore(this).setInstalledPackages(packages)
    }
}
