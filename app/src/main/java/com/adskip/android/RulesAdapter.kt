package com.adskip.android

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.adskip.android.databinding.ItemRuleBinding

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable,
    var keywordsRaw: String
)

class RulesAdapter(
    private val onKeywordsChanged: (packageName: String, rawKeywords: String) -> Unit
) : RecyclerView.Adapter<RulesAdapter.RuleViewHolder>() {

    private var items: List<AppEntry> = emptyList()

    fun submitList(list: List<AppEntry>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRuleBinding.inflate(inflater, parent, false)
        return RuleViewHolder(binding, onKeywordsChanged)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class RuleViewHolder(
        private val binding: ItemRuleBinding,
        private val onKeywordsChanged: (packageName: String, rawKeywords: String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentPackage: String? = null
        private var currentEntry: AppEntry? = null

        init {
            binding.etKeywords.setOnClickListener {
                showEditDialog()
            }
        }

        fun bind(entry: AppEntry) {
            currentEntry = entry
            currentPackage = entry.packageName
            binding.ivIcon.setImageDrawable(entry.icon)
            binding.tvName.text = entry.label
            binding.tvPackage.text = entry.packageName
            if (binding.etKeywords.text.toString() != entry.keywordsRaw) {
                binding.etKeywords.setText(entry.keywordsRaw)
            }
        }

        private fun showEditDialog() {
            val entry = currentEntry ?: return
            val pkg = currentPackage ?: return
            val context = binding.root.context
            val input = EditText(context).apply {
                setText(entry.keywordsRaw)
                hint = "关键字（逗号分隔）"
                setSelection(text.length)
            }
            AlertDialog.Builder(context)
                .setTitle(entry.label)
                .setView(input)
                .setPositiveButton("保存") { _, _ ->
                    val raw = input.text.toString()
                    entry.keywordsRaw = raw
                    binding.etKeywords.setText(raw)
                    onKeywordsChanged(pkg, raw)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
}

