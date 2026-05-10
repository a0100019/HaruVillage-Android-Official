package com.a0100019.mypat.presentation.main.management

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {

    // 선택된 이웃 유저 ID (NeighborScreen → NeighborInformationScreen 등에서 공유)
    private val _selectedUserId = MutableStateFlow("")
    val selectedUserId: StateFlow<String> = _selectedUserId.asStateFlow()

    // 현재 로그인 유저 이름 (여러 화면에서 참조)
    private val _currentUserName = MutableStateFlow("")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    // 하단 네비게이션 선택 탭
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectUser(userId: String) {
        _selectedUserId.value = userId
    }

    fun setCurrentUserName(name: String) {
        _currentUserName.value = name
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

}
