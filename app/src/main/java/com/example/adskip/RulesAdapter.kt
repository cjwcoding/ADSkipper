package com.example.adskip

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import com.example.adskip.databinding.ItemRuleBinding

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
            binding.etKeywords.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveIfChanged()
                    true
                } else {
                    false
                }
            }
            binding.etKeywords.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    saveIfChanged()
                }
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

        private fun saveIfChanged() {
            val entry = currentEntry ?: return
            val raw = binding.etKeywords.text.toString()
            if (raw != entry.keywordsRaw) {
                entry.keywordsRaw = raw
                val pkg = currentPackage ?: return
                onKeywordsChanged(pkg, raw)
            }
        }
    }
}
