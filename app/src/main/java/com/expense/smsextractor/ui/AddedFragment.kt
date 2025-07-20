package com.expense.smsextractor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.expense.smsextractor.ExpenseAdapter
import com.expense.smsextractor.ExpenseViewModel
import com.expense.smsextractor.ExpenseViewModelFactory
import com.expense.smsextractor.data.AppDatabase
import com.expense.smsextractor.data.MonthYear
import com.expense.smsextractor.databinding.FragmentAddedBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddedFragment : Fragment() {

    private var _binding: FragmentAddedBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ExpenseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val viewModelFactory = ExpenseViewModelFactory(database)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ExpenseViewModel::class.java]

        val expenseAdapter = ExpenseAdapter(
            isDraft = false,
            fragmentManager = childFragmentManager,
            onAddClick = {},
            onSendBackClick = { expenseId ->
                viewModel.sendBackToDraft(expenseId)
            }
        )

        binding.addedRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = expenseAdapter
        }

        // Setup month filter
        viewModel.uniqueMonths.observe(viewLifecycleOwner) { months ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.monthFilterSpinner.adapter = adapter
        }

        viewModel.selectedMonth.observe(viewLifecycleOwner) { selectedMonth ->
            binding.monthFilterSpinner.setSelection(
                viewModel.uniqueMonths.value?.indexOf(selectedMonth) ?: 0
            )
            // Refresh expenses when month changes
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.addedExpenses.collectLatest { pagingData ->
                    expenseAdapter.submitData(pagingData)
                }
            }
        }

        binding.monthFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedMonth = parent?.getItemAtPosition(position) as MonthYear
                viewModel.selectMonth(selectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.selectMonth(MonthYear.DEFAULT)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addedExpenses.collectLatest { pagingData ->
                expenseAdapter.submitData(pagingData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}