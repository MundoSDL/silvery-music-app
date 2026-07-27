package com.silverymusic.app.ui.screens.profileswitcher

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

data class ProfileSwitcherUiState(
    val profiles: List<Profile> = emptyList(),
    val activeProfileId: String = "",
)

class ProfileSwitcherViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSwitcherUiState())
    val uiState: StateFlow<ProfileSwitcherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.profiles, repository.activeProfileId) { profiles, activeId ->
                ProfileSwitcherUiState(profiles = profiles, activeProfileId = activeId)
            }.collect { state -> _uiState.update { state } }
        }
    }

    fun onSelectProfile(profileId: String) = repository.selectProfile(profileId)
}
