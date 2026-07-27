package com.silverymusic.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/** No Hilt in this build — screens construct their ViewModel against [com.silverymusic.app.data.AppContainer] through this. */
@Composable
inline fun <reified VM : ViewModel> silveryViewModel(crossinline create: () -> VM): VM =
    viewModel(factory = viewModelFactory { initializer { create() } })
