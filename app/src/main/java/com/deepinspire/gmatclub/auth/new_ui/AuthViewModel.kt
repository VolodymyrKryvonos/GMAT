package com.deepinspire.gmatclub.auth.new_ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deepinspire.gmatclub.api.AuthException
import com.deepinspire.gmatclub.storage.IStorage.ICallbackAuth
import com.deepinspire.gmatclub.storage.Repository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: Repository) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _error = MutableSharedFlow<AuthException>()
    val error = _error.asSharedFlow()

    private val _forgotPasswordSuccess = MutableSharedFlow<String>()
    val forgotPasswordSuccess = _forgotPasswordSuccess.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun logged(context: Context): Boolean {
        return repository.logged(context)
    }

    fun signIn(context: Context, username: String, password: String) {
        _isLoading.value = true
        repository.signIn(username, password, context, object : ICallbackAuth {
            override fun onSuccess() {
                _isLoggedIn.value = true
                _isLoading.value = false
            }

            override fun onError(exception: AuthException) {
                viewModelScope.launch {
                    _error.emit(exception)
                    _isLoading.value = false
                }
            }
        })
    }

    fun signInSocial(
        context: Context,
        provider: String,
        idToken: String,
        accessToken: String,
        expiresIn: String
    ) {
        _isLoading.value = true
        repository.signInSocial(
            provider,
            idToken,
            accessToken,
            expiresIn,
            context,
            object : ICallbackAuth {
                override fun onSuccess() {
                    Log.e("signInSocial", logged(context).toString())
                    _isLoggedIn.value = true
                    _isLoading.value = false
                }

                override fun onError(exception: AuthException) {
                    viewModelScope.launch {
                        val ex = AuthException(Exception("Failed sign in $provider"), provider)
                        _error.emit(ex)
                        _isLoading.value = false
                    }
                }
            })
    }

    fun forgotPassword(email: String?) {
        _isLoading.value = true
        repository.forgotPassword(email!!, object : ICallbackAuth {
            override fun onSuccess() {
                viewModelScope.launch {
                    _isLoading.value = false
                    _forgotPasswordSuccess.emit("forgotPassword")
                }
            }

            override fun onError(exception: AuthException) {
                viewModelScope.launch {
                    exception.printStackTrace()
                    _error.emit(exception)
                    _isLoading.value = false
                }
            }
        })
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}


class AuthViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}