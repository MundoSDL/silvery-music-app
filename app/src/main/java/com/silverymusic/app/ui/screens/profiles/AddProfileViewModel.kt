package com.silverymusic.app.ui.screens.profiles

import androidx.lifecycle.ViewModel
import com.silverymusic.app.data.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AddProfileUiState(
    val name: String = "",
    val isKid: Boolean = false,
    val accentIndex: Int = 0,
) {
    val canCreate: Boolean get() = name.isNotBlank()
}

class AddProfileViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProfileUiState())
    val uiState: StateFlow<AddProfileUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onKidChange(isKid: Boolean) = _uiState.update { it.copy(isKid = isKid) }
    fun onAccentSelected(index: Int) = _uiState.update { it.copy(accentIndex = index) }

    fun onCreate() {
        val state = _uiState.value
        if (!state.canCreate) return
        repository.addProfile(state.name.trim(), state.isKid, state.accentIndex)
    }
}
