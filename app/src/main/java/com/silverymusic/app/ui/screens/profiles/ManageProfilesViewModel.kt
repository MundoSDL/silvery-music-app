package com.silverymusic.app.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageProfilesUiState(
    val profiles: List<Profile> = emptyList(),
    val activeProfileId: String = "",
    /** Non-null while the confirm-removal dialog is showing for that profile. */
    val pendingRemoval: Profile? = null,
)

class ManageProfilesViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageProfilesUiState())
    val uiState: StateFlow<ManageProfilesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.profiles, repository.activeProfileId) { profiles, activeId ->
                profiles to activeId
            }.collect { (profiles, activeId) ->
                _uiState.update { it.copy(profiles = profiles, activeProfileId = activeId) }
            }
        }
    }

    fun onRemoveRequested(profile: Profile) {
        _uiState.update { it.copy(pendingRemoval = profile) }
    }

    fun onRemovalDismissed() {
        _uiState.update { it.copy(pendingRemoval = null) }
    }

    fun onRemovalConfirmed() {
        _uiState.value.pendingRemoval?.let { repository.removeProfile(it.id) }
        _uiState.update { it.copy(pendingRemoval = null) }
    }
}
