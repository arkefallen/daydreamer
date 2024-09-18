package com.android.ark.write

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.android.ark.model.Diary
import com.android.ark.ui.components.DisplayAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteTopBar(
    onBackPressed: () -> Unit,
    selectedDiary: Diary?,
    onDeleteClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = if (selectedDiary != null) "Diary Detail" else "New Diary",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back Button"
                )
            }
        },
        actions = {
            if (selectedDiary?.title != null) {
                WriteMenuAction(
                    selectedDiary = selectedDiary,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    )
}

@Composable
fun WriteMenuAction(
    selectedDiary: Diary?,
    onDeleteClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var openDialog by remember { mutableStateOf(false) }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(text = "Delete") },
            onClick = {
                openDialog = true
                expanded = false
            }
        )
    }
    DisplayAlertDialog(
        title = "Remove Diary",
        message = "Are you sure want to permanently delete this diary note '${selectedDiary?.title}'?",
        dialogOpened = openDialog,
        onDialogClosed = { openDialog = false },
        onYesClicked = {
            onDeleteClick()
            openDialog = false
        }
    )
    IconButton(onClick = { expanded = !expanded }) {
        Icon(
            imageVector = Icons.Rounded.MoreVert,
            contentDescription = "Overflow Menu Icon",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}