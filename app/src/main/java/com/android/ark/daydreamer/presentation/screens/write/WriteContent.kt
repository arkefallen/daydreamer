package com.android.ark.daydreamer.presentation.screens.write

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.ark.daydreamer.model.Diary
import com.android.ark.daydreamer.model.GalleryImage
import com.android.ark.daydreamer.model.Mood
import com.android.ark.daydreamer.presentation.components.GalleryState
import com.android.ark.daydreamer.presentation.components.GalleryUploader
import com.android.ark.daydreamer.utils.toInstant
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockConfig
import com.maxkeppeler.sheets.clock.models.ClockSelection
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WriteContent(
    paddingValues: PaddingValues,
    pagerState: PagerState,
    title: String,
    onTitleChanged: (String) -> Unit,
    description: String,
    onDescriptionChanged: (String) -> Unit,
    selectedDiary: Diary?,
    onSaveClicked: (Diary) -> Unit,
    onUpdatedDateTime: (ZonedDateTime) -> Unit,
    galleryState: GalleryState,
    onImageSelected: (Uri) -> Unit,
    onImageClicked: (GalleryImage) -> Unit,
    onAddImageClicked: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val calendarState = rememberUseCaseState()
    val clockState = rememberUseCaseState()

    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    val formattedDate = remember(key1 = currentDate) {
        DateTimeFormatter
            .ofPattern("d MMM yyyy")
            .format(currentDate)
    }
    val formattedTime = remember(key1 = currentTime) {
        DateTimeFormatter
            .ofPattern("hh:mm: a")
            .format(currentTime)
    }
    val selectedDiaryDateTime = remember(selectedDiary) {
        if (selectedDiary != null) {
            SimpleDateFormat("d MMM yyyy, hh:mm a", Locale.getDefault())
                .format(Date.from(selectedDiary.date.toInstant()))
        } else {
            "Unknown"
        }
    }
    var updatedDateTime by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val pagerIndex = pagerState.currentPage

    CalendarDialog(
        state = calendarState,
        selection = CalendarSelection.Date { selectionDate ->
            currentDate = selectionDate
            clockState.show()
        },
        config = CalendarConfig(
            monthSelection = true,
            yearSelection = true,
            style = CalendarStyle.MONTH,
        )
    )

    ClockDialog(
        state = clockState,
        selection = ClockSelection.HoursMinutes { selectionHour, selectionMinute ->
            currentTime = LocalTime.of(selectionHour, selectionMinute)
            updatedDateTime = true
        },
        config = ClockConfig(
            is24HourFormat = false
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = paddingValues.calculateTopPadding())
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
        ) { itemIndex ->
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    modifier = Modifier.size(120.dp),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Mood.entries[itemIndex].icon)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Mood Entries"
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "How is your feeling?",
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = Mood.entries[pagerIndex].name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 24.dp)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            OutlinedTextField(
                value = if (selectedDiary != null && !updatedDateTime) selectedDiaryDateTime
                else "$formattedDate, $formattedTime",
                onValueChange = {},
                modifier = Modifier
                    .clickable(
                        onClick = { calendarState.show() },
                        enabled = true
                    )
                    .fillMaxWidth(),
                suffix = {
                    if (updatedDateTime) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Date Close Button",
                            modifier = Modifier.clickable(
                                onClick = {
                                    updatedDateTime = false
                                    currentDate = LocalDate.now()
                                    currentTime = LocalTime.now()
                                }
                            )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.DateRange,
                            contentDescription = "Date Button",
                            modifier = Modifier.clickable(onClick = { calendarState.show() })
                        )
                    }
                },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    disabledSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChanged,
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(text = "Your diary title")
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                maxLines = 1,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChanged,
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(text = "Tell me about your day")
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                ),
            )
        }
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            GalleryUploader(
                galleryState = galleryState,
                onImageSelected = onImageSelected,
                onImageClicked = onImageClicked,
                onAddImageClicked = {
                    focusManager.clearFocus()
                    onAddImageClicked()
                }
            )
            Button(
                onClick = {
                    if (title.isNotEmpty() && description.isNotEmpty()) {
                        onUpdatedDateTime(
                            ZonedDateTime.of(
                                currentDate,
                                currentTime,
                                ZoneId.systemDefault()
                            )
                        )
                        onSaveClicked(
                            Diary().apply {
                                this.title = title
                                this.description = description
                                this.mood = Mood.entries[pagerState.currentPage].name
                            }
                        )
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.small,
                enabled = !(title.isEmpty() || description.isEmpty())
            ) {
                if (selectedDiary != null) {
                    Text(text = "Update", fontWeight = FontWeight.Bold)
                } else {
                    Text(text = "Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview(showBackground = true)
fun WriteContentPreview() {
//    WriteContent(
//        paddingValues = PaddingValues(),
//        pagerState = rememberPagerState(pageCount = { 1 }),
//        title = "title",
//        onTitleChanged = {
//
//        },
//        description = "description",
//        onDescriptionChanged = {},
//        selectedDiary = null,
//        onSaveClicked = {
//
//        },
//        onUpdatedDateTime = {}
//    )
}