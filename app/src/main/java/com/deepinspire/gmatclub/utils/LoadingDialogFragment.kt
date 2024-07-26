package com.deepinspire.gmatclub.utils

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.deepinspire.gmatclub.databinding.LoadingLayoutBinding

class LoadingDialogFragment : DialogFragment() {

    private var _binding: LoadingLayoutBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoadingLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler.postDelayed({
            dismiss()
        }, 20000)
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setCancelable(false)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
        dialog?.setCancelable(true)
    }

    companion object {
        private var dialog: LoadingDialogFragment? = null

        fun showFullScreenDialog(supportFragmentManager: FragmentManager) {
            if (dialog == null) {
                dialog = LoadingDialogFragment()
            }
            if (dialog?.isAdded == false)
                dialog?.show(supportFragmentManager, "LoadingDialogFragment")
        }

        fun dismiss() {
            dialog?.dismiss()
            dialog = null
        }
    }
}