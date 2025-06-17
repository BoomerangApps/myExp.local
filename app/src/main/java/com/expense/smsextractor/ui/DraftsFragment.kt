package com.expense.smsextractor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.expense.smsextractor.ExpenseAdapter
import com.expense.smsextractor.ExpenseViewModel
import com.expense.smsextractor.ExpenseViewModelFactory
import com.expense.smsextractor.data.AppDatabase
import com.expense.smsextractor.databinding.FragmentDraftsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DraftsFragment : Fragment() {

    private var _binding: FragmentDraftsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ExpenseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDraftsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val viewModelFactory = ExpenseViewModelFactory(database)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ExpenseViewModel::class.java]

        val expenseAdapter = ExpenseAdapter(isDraft = true) { expenseId ->
            viewModel.addExpense(expenseId)
        }

        binding.draftsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = expenseAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.draftExpenses.collectLatest { pagingData ->
                expenseAdapter.submitData(pagingData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}