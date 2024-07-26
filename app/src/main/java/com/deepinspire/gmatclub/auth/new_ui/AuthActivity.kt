package com.deepinspire.gmatclub.auth.new_ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.deepinspire.gmatclub.R
import com.deepinspire.gmatclub.api.Api
import com.deepinspire.gmatclub.databinding.ActivityAuthNewBinding
import com.deepinspire.gmatclub.notifications.Notifications
import com.deepinspire.gmatclub.storage.Injection
import com.deepinspire.gmatclub.utils.LoadingDialogFragment
import com.deepinspire.gmatclub.utils.Storage
import com.deepinspire.gmatclub.web.WebActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date


class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthNewBinding
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            Injection.getRepository(
                this
            )
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthNewBinding.inflate(layoutInflater)
        val view = binding.root


        handleLogin()

        setContentView(view)
        setupObservers()
        setupViews()
    }


    private fun handleLogin() {
        if (authViewModel.logged(this)) {
            openWebSite(Api.FORUM_URL)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this@AuthActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                200
            )
        }
        if (!TextUtils.isEmpty(
                Storage.getGoogleIdToken(
                    applicationContext
                )
            ) &&
            !TextUtils.isEmpty(Storage.getGoogleAccessToken(applicationContext))
        ) {
            val expiresIn = Date().time + 432000000
            authViewModel.signInSocial(
                this, "google", Storage.getGoogleIdToken(
                    applicationContext
                ), Storage.getGoogleAccessToken(applicationContext), expiresIn.toString()
            )
        }

        if (!TextUtils.isEmpty(
                Storage.getFacebookIdToken(
                    applicationContext
                )
            ) &&
            !TextUtils.isEmpty(Storage.getFacebookAccessToken(applicationContext))
        ) {
            val expiresIn = Date().time + 432000000
            authViewModel.signInSocial(
                this, "facebook", Storage.getFacebookIdToken(
                    applicationContext
                ), Storage.getFacebookAccessToken(applicationContext), expiresIn.toString()
            )
        }

        if (!TextUtils.isEmpty(
                Storage.getLoginEmail(
                    applicationContext
                )
            ) &&
            !TextUtils.isEmpty(Storage.getLoginPassword(applicationContext))
        ) {
            authViewModel.signIn(
                this, Storage.getLoginEmail(
                    applicationContext
                ), Storage.getLoginPassword(applicationContext)
            )
        }
    }

    private fun setupViews() {
        binding.registerButton.setOnClickListener {
            openWebSite(Api.FORUM_REGISTER_URL)
        }

        binding.passwordField.doOnTextChanged { _, _, _, _ ->
            binding.passwordLayout.error = ""
        }

        binding.emailField.doOnTextChanged { _, _, _, _ ->
            binding.emailLayout.error = ""
        }

        binding.loginButton.setOnClickListener {
            var error = false
            if (binding.passwordField.text.toString().length < 8) {
                binding.passwordLayout.error = getString(R.string.password_too_short)
                error = true
            }
            error = !validateEmailField() || error

            if (!error) {
                authViewModel.signIn(
                    this,
                    binding.emailField.text.toString(),
                    binding.passwordField.text.toString()
                )
            }
        }

        binding.submitButton.setOnClickListener {
            if (validateEmailField()) {
                authViewModel.forgotPassword(binding.emailField.text.toString())
            }
        }

        binding.forgotPasswordButton.setOnClickListener {
            collapseToCenter(binding.signInFields)
            expandFromCenter(binding.forgotPassFields)

            binding.screenLabel.text = getString(R.string.forgot_password)
        }

        binding.switchToSignIn.setOnClickListener {
            expandFromCenter(binding.signInFields)
            collapseToCenter(binding.forgotPassFields)

            binding.screenLabel.text = getString(R.string.log_in_or_sign_up)
        }
    }

    private fun validateEmailField(): Boolean {
        if (!authViewModel.isValidEmail(binding.emailField.text.toString())) {
            binding.emailLayout.error = getString(R.string.invalid_email)
            return false
        }
        return true
    }


    fun expandFromCenter(v: View) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val targetHeight = v.measuredHeight
        v.layoutParams.height = 0
        v.visibility = View.VISIBLE

        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                if (interpolatedTime == 1f) {
                    v.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    v.layoutParams.height = (targetHeight * interpolatedTime).toInt()
                }
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        a.duration = (targetHeight * 1.5 / v.context.resources.displayMetrics.density).toLong()
        v.startAnimation(a)
    }

    fun collapseToCenter(v: View) {
        val initialHeight = v.measuredHeight

        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                if (interpolatedTime == 1f) {
                    v.isVisible = false
                } else {
                    val newHeight = initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.layoutParams.height = newHeight
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        a.duration = (initialHeight * 1.5 / v.context.resources.displayMetrics.density).toLong()
        v.startAnimation(a)
    }

    fun openWebSite(url: String) {
        val intent = Intent(this@AuthActivity, WebActivity::class.java)
        intent.setData(Uri.parse(url))
        intent.putExtra(Notifications.INPUT_URL, url)
        startActivity(intent)
        finish()
    }


    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                authViewModel.error.collectLatest {
                    Snackbar.make(binding.root, it.localizedMessage, Snackbar.LENGTH_LONG).show()
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                authViewModel.isLoggedIn.collectLatest {
                    if (it) {
                        openWebSite(Api.FORUM_URL)
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                authViewModel.forgotPasswordSuccess.collectLatest {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.new_pass_was_sent),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                authViewModel.isLoading.collectLatest {
                    if (it) {
                        LoadingDialogFragment.showFullScreenDialog(supportFragmentManager)
                    } else {
                        LoadingDialogFragment.dismiss()
                    }
                }
            }
        }
    }
}