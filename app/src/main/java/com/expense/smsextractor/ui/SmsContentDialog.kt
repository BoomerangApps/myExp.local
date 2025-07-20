package com.expense.smsextractor.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.expense.smsextractor.R
import com.expense.smsextractor.databinding.DialogSmsContentBinding

class SmsContentDialog : DialogFragment() {
    
    private var _binding: DialogSmsContentBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val ARG_SMS_CONTENT = "sms_content"
        
        fun newInstance(smsContent: String): SmsContentDialog {
            return SmsContentDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_SMS_CONTENT, smsContent)
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSmsContentBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arguments?.getString(ARG_SMS_CONTENT)?.let { smsContent ->
            binding.smsContentText.text = smsContent
        }
        
        // Close dialog when clicking outside
        binding.root.setOnClickListener {
            dismiss()
        }
        
        // Prevent inner clicks from dismissing the dialog
        binding.dialogContent.setOnClickListener {
            // Do nothing - prevent clicks on the content from closing the dialog
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
