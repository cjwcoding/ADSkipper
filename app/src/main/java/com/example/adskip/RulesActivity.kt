package com.example.adskip

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adskip.databinding.ActivityRulesBinding

class RulesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRulesBinding
    private lateinit var adapter: RulesAdapter
    private val ruleStore by lazy { RuleStore(this) }
    private var allApps: List<AppEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupList()
        loadApps()
        setupSearch()
    }

    private fun setupList() {
        adapter = RulesAdapter { packageName, rawKeywords ->
            ruleStore.setPackageKeywordsRaw(packageName, rawKeywords)
        }
        binding.rvRules.layoutManager = LinearLayoutManager(this)
        binding.rvRules.adapter = adapter
    }

    private fun loadApps() {
        val apps = AppScanner.scanLauncherApps(this)
        ruleStore.setInstalledPackages(apps.map { it.packageName }.toSet())
        allApps = apps.map { app ->
            AppEntry(
                packageName = app.packageName,
                label = app.label,
                icon = app.icon,
                keywordsRaw = ruleStore.getPackageKeywordsRaw(app.packageName)
            )
        }
        adapter.submitList(allApps)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                if (query.isEmpty()) {
                    adapter.submitList(allApps)
                } else {
                    val filtered = allApps.filter {
                        it.label.contains(query, ignoreCase = true) ||
                            it.packageName.contains(query, ignoreCase = true)
                    }
                    adapter.submitList(filtered)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
