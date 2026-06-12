package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TimeLeftViewModel
import com.example.ui.components.CircularProgress
import com.example.ui.components.TaskItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TimeLeftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todaysTasks by viewModel.todaysTasks.collectAsStateWithLifecycle()
    val completedCount by viewModel.tasksCompletedCount.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalTasksCount.collectAsStateWithLifecycle()

    var taskTitle by remember { mutableStateOf("") }
    val categories = listOf("Work", "Personal", "Health", "Learning")
    var selectedCategory by remember { mutableStateOf("Work") }

    // Enforce 10 tasks limit
    val canAddTask = totalCount < 10

    // Sorted task list: incomplete first, completed last
    val sortedTasks = remember(todaysTasks) {
        todaysTasks.sortedWith(compareBy<com.example.data.Task> { it.completed }.thenByDescending { it.createdAt })
    }

    val progressRatio = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // SECTION A: TITLE TITLE
            Text(
                text = "Tasks",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // SECTION B: PROGRESS CARD
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(16.dp))
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgress(
                    size = 76.dp,
                    progress = progressRatio,
                    strokeWidth = 8.dp,
                    color = Accent,
                    trackColor = Border
                ) {
                    Text(
                        text = "${(progressRatio * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$completedCount",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " / $totalCount",
                            color = TextMuted,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = "tasks completed today",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // SECTION C: ADD TASK CARD (Tabs + Input)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(16.dp))
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                // Category tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val catColor = getCategoryColor(cat)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(if (isSelected) catColor else SurfaceAlt)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) AccentText else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input + Add Button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = taskTitle,
                        onValueChange = { if (it.length <= 40) taskTitle = it },
                        placeholder = { Text("What needs to be done?", color = TextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceAlt,
                            unfocusedContainerColor = SurfaceAlt,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    val isInputValid = taskTitle.isNotBlank()
                    val buttonBg = if (isInputValid) Accent else SurfaceAlt
                    val buttonTint = if (isInputValid) AccentText else TextDim

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(buttonBg)
                            .clickable {
                                if (isInputValid) {
                                    if (canAddTask) {
                                        viewModel.addTask(taskTitle.trim(), selectedCategory)
                                        taskTitle = ""
                                        Toast.makeText(context, "Task added!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Limit of 10 tasks reached for today!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = buttonTint
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // SECTION D: TASK LIST
        if (sortedTasks.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "No tasks yet",
                        tint = Border,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tasks added yet today.\nPlan your day above!",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            items(sortedTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onCompleteToggle = { viewModel.toggleTaskCompletion(task) },
                    onIncrementPomodoro = { viewModel.incrementTaskPomodoro(task) },
                    onDelete = { viewModel.deleteTask(task) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
