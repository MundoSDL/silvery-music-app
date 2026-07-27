package com.silverymusic.app.data.model

sealed interface ListeningStatus {
    data object Solo : ListeningStatus
    data class Synced(val friendName: String) : ListeningStatus
}
