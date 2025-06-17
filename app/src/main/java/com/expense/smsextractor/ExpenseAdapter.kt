package com.expense.smsextractor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.expense.smsextractor.data.ExpenseEntity
import com.expense.smsextractor.databinding.ItemExpenseBinding

class ExpenseAdapter(
    private val isDraft: Boolean,
    private val onAddClick: (Long) -> Unit
) : PagingDataAdapter<ExpenseEntity, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = getItem(position)
        if (expense == null) {
            // The Paging library may ask for an item that is not yet loaded.
            // We can show a placeholder or do nothing for now.
            return
        }
        holder.bind(expense, isDraft, onAddClick)
    }

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: ExpenseEntity, isDraft: Boolean, onAddClick: (Long) -> Unit) {
            binding.amountText.text = "₹${expense.amount}"
            binding.descriptionText.text = expense.description
            binding.dateText.text = expense.date
            binding.senderText.text = expense.sender

            if (isDraft) {
                binding.addButton.visibility = View.VISIBLE
                binding.addButton.setOnClickListener {
                    onAddClick(expense.id)
                }
            } else {
                binding.addButton.visibility = View.GONE
            }
        }
    }
}

class ExpenseDiffCallback : DiffUtil.ItemCallback<ExpenseEntity>() {
    override fun areItemsTheSame(oldItem: ExpenseEntity, newItem: ExpenseEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ExpenseEntity, newItem: ExpenseEntity): Boolean {
        return oldItem == newItem
    }
}