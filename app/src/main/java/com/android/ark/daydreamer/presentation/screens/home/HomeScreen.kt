package com.android.ark.daydreamer.presentation.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.android.ark.daydreamer.R
import com.android.ark.daydreamer.data.repository.Diaries
import com.android.ark.daydreamer.utils.RequestState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    diaries: Diaries,
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    onMenuClick: () -> Unit,
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    viewmodel: HomeViewmodel,
    onDeleteAllDiaries: () -> Unit
) {
    val padding by remember { mutableStateOf(PaddingValues()) }
    // Define ScrollBehavior for nesting DateHeader at the top of the screen during scroll down
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    NavigationDrawer(
        drawerState = drawerState,
        onSignOutClicked = onSignOutClicked,
        onDeleteAllDiaries = onDeleteAllDiaries
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HomeTopBar(onMenuClicked = onMenuClick, scrollBehavior = scrollBehavior)
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = navigateToWrite,
                    modifier = Modifier.padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                ) {
                    Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit")
                }
            }
        ) {
            when (diaries) {
                is RequestState.Success -> {
                    HomeContent(
                        diaryNotes = diaries.data,
                        onClick = navigateToWriteWithArgs,
                        paddingValues = it,
                        viewmodel = viewmodel
                    )
                }
                is RequestState.Error -> {
                    EmptyPage(
                        title = "Error",
                        subtitle = diaries.message
                    )
                }
                is RequestState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    EmptyPage(
                        title = "No Data",
                        subtitle = "No diaries exist"
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    onDeleteAllDiaries: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_daydreamer),
                        contentDescription = "Daydreamer",
                        modifier = Modifier.size(250.dp)
                    )
                }
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete All Diaries",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Delete All Diaries", color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    selected = false,
                    onClick = onDeleteAllDiaries
                )
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.google_logo),
                                contentDescription = "Google",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Sign Out", color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    selected = false,
                    onClick = onSignOutClicked
                )
            }
        },
        content = content
    )
}