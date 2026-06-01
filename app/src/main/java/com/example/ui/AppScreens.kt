package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun AppNavigationWrapper(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        topBar = {
            TopAppBarComponent(studentProfile)
        },
        bottomBar = {
            BottomNavigationBarComponent(
                currentScreen = currentScreen,
                onTabSelected = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(viewModel)
                    Screen.SYLLABUS -> SyllabusScreen(viewModel)
                    Screen.TASKS -> TasksScreen(viewModel)
                    Screen.GROUPS -> GroupsScreen(viewModel)
                    Screen.PROFILE -> ProfileScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarComponent(profile: StudentProfile?) {
    val studentName = profile?.name ?: "Student"
    val studentBranch = profile?.branch ?: "CSE"
    val studentSem = profile?.semester ?: 6

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    //南亚学生头像 Vector placeholder or Avatar
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Portrait Avatar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        text = "EduAI Assistant",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$studentName | $studentBranch Sem $studentSem",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = {},
                modifier = Modifier.testTag("action_magic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "AI Sparkle",
                    tint = SecondaryPurple
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CardSurfaceWhite,
            scrolledContainerColor = CardSurfaceWhite
        ),
        modifier = Modifier.border(width = 0.5.dp, color = OutlineVariantBorder)
    )
}

@Composable
fun BottomNavigationBarComponent(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = CardSurfaceWhite,
        tonalElevation = 4.dp,
        modifier = Modifier.height(68.dp)
    ) {
        val navItems = listOf(
            NavItem(Screen.HOME, Icons.Default.Home, "Home"),
            NavItem(Screen.SYLLABUS, Icons.Default.Book, "Syllabus"),
            NavItem(Screen.TASKS, Icons.Default.List, "Tasks"),
            NavItem(Screen.GROUPS, Icons.Default.Groups, "Groups"),
            NavItem(Screen.PROFILE, Icons.Default.Person, "Profile")
        )

        navItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SecondaryPurple else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SecondaryPurple,
                    selectedIconColor = Color.White,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

data class NavItem(val screen: Screen, val icon: ImageVector, val label: String)

// ==========================================
// 1. HOME / DASHBOARD SCREEN
// ==========================================
@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val studentProfile by viewModel.studentProfile.collectAsState()
    val tasksFinished by viewModel.tasksFinishedCount.collectAsState()
    val totalTasks by viewModel.totalTasksCount.collectAsState()
    val studyTasks by viewModel.studyTasks.collectAsState()

    var showQuizDialog by remember { mutableStateOf(false) }
    var showExplanationDialog by remember { mutableStateOf(false) }
    var activeSummarizeDialogText by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and streak badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome, ${studentProfile?.name ?: "Rahul"}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Ready to tackle your ${studentProfile?.currentFocusSubject ?: "Mechanics"} module today?",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Fire state streak
                Row(
                    modifier = Modifier
                        .background(ChipBgGrey, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Streak Fire",
                        tint = AcademicGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${studentProfile?.streakDays ?: 12} Day Streak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Bento Section: Today's Focus and Circular Semester progress side-by-side on broad canvases, stacked logically
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's Focus List (Thermodynamics, Labs, Summarize DBMS)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = "Focus Analytic Icon",
                                    tint = SecondaryPurple
                                )
                                Text(
                                    text = "Today's Focus",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "3 remaining",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .background(
                                        SecondaryPurple.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Divider(color = OutlineVariantBorder)

                        // Focus item 1: Thermodynamics
                        FocusItemRow(
                            title = "Thermodynamics Assignment",
                            detail = "Due 11:59 PM",
                            badgeText = "URGENT",
                            badgeColor = ErrorCrimson,
                            badgeBgColor = ErrorContainerCrimson,
                            isCompleted = false
                        )

                        // Focus item 2: Lab report
                        FocusItemRow(
                            title = "Lab Report: Microprocessors",
                            detail = "12 pages total",
                            badgeText = "CORE",
                            badgeColor = MaterialTheme.colorScheme.primary,
                            badgeBgColor = ChipBgGrey,
                            isCompleted = false
                        )

                        // Focus item 3: Summarize DBMS Unit 4 with "EXECUTE AI"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ChipBgGrey.copy(alpha = 0.5f))
                                .border(0.5.dp, SecondaryPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Summarize DBMS Unit 4",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = "#Database",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .background(Color.White, RoundedCornerShape(4.dp))
                                                .border(0.5.dp, OutlineVariantBorder, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                        Text(
                                            text = "AI Task",
                                            fontSize = 9.sp,
                                            color = SecondaryPurple,
                                            modifier = Modifier
                                                .background(SecondaryPurple.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        activeSummarizeDialogText = "DBMS Unit 4 covers Transactions & Concurrency Control, focusing on ACID Properties, Serializability, and lock-based protocols for preserving database integrity during parallel reads/writes."
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryPurple),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("execute_ai_dbms_button")
                                ) {
                                    Text("EXECUTE AI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Semester Progress card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Semester Progress",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Circular Progress Representation
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(130.dp)
                        ) {
                            val percent = studentProfile?.overallProgress ?: 70
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = OutlineVariantBorder,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = SecondaryPurple,
                                    startAngle = -90f,
                                    sweepAngle = (percent / 100f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$percent%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "COMPLETED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "\"You're 5% ahead of your peers in Mathematics III!\"",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        // Pill segments
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(SuccessGreen, SuccessGreen, SuccessGreen, OutlineVariantBorder).forEach { col ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Check-In Prompt Ask
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Daily Check-In",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryPurple,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Hey! What's your focus for today — should I pull up your tasks, check your WhatsApp group updates, or review your syllabus progress?",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = viewModel.getMotivationalOneLiner(),
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = OnSecondaryContainerPurple,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Quick Actions Grid (UPLOAD, QUIZ, SUMMARY, PLAN)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.CloudUpload,
                        title = "UPLOAD SYLLABUS",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_upload"),
                        onClick = { viewModel.navigateTo(Screen.SYLLABUS) }
                    )
                    QuickActionCard(
                        icon = Icons.Default.Psychology,
                        title = "QUIZ ME",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_quiz"),
                        onClick = { showQuizDialog = true }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Summarize,
                        title = "AI SUMMARY",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            activeSummarizeDialogText = "Syllabus Status: You are currently on track for Week 1 Units. Your peak weightage topic is Infinite Series & Fourier Transforms, containing 25% final examination marks. Recommended study source is NPTEL Unit 4."
                        }
                    )
                    QuickActionCard(
                        icon = Icons.Default.EventNote,
                        title = "STUDY PLAN",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TASKS) }
                    )
                }
            }
        }

        // Recent AI Insight
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recent AI Insight",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Based on your recent practice tests, you're struggling with 'Laplace Transforms'. Would you like a simplified breakdown?",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showExplanationDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("START LEARNING", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Insight dismissed.", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("NOT NOW", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Insight Bulb",
                        tint = SecondaryPurple,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // High resolution student collaborate image banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?auto=format&fit=crop&w=600&q=80",
                        contentDescription = "Study community banner background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Study Groups",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2 active sessions in Calculus III",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                            Row(
                                modifier = Modifier.clickable {
                                    Toast.makeText(context, "Joined Calculus III live room!", Toast.LENGTH_SHORT).show()
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "JOIN NOW",
                                    color = OnSecondaryContainerPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Arrow right icon",
                                    tint = OnSecondaryContainerPurple,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialouge implementations to provide incredibly rich interaction feel
    if (activeSummarizeDialogText != null) {
        Dialog(onDismissRequest = { activeSummarizeDialogText = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Academic Breakdown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SecondaryPurple
                        )
                        IconButton(onClick = { activeSummarizeDialogText = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close breakdown")
                        }
                    }
                    Text(
                        text = activeSummarizeDialogText ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                    Button(
                        onClick = { activeSummarizeDialogText = null },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Understood")
                    }
                }
            }
        }
    }

    if (showQuizDialog) {
        var quizAnswered by remember { mutableStateOf(false) }
        var selectedAnswerIndex by remember { mutableStateOf(-1) }

        Dialog(onDismissRequest = { showQuizDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = "Brain", tint = SecondaryPurple)
                            Text(
                                text = "BTech Math Quick Quiz",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showQuizDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close quiz")
                        }
                    }

                    Text(
                        text = "Question: What is Dirichlet's condition for a function f(x) to expand in a Fourier Series?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )

                    val options = listOf(
                        "f(x) must be continuous everywhere with no bounds",
                        "f(x) must have a finite number of discontinuities and maxima/minima within a period",
                        "f(x) must be a polynomial function of degree 3 or more",
                        "f(x) must have infinite derivatives across all quadrants"
                    )

                    options.forEachIndexed { idx, op ->
                        val isSelected = selectedAnswerIndex == idx
                        val containerBg = if (quizAnswered) {
                            if (idx == 1) SuccessContainerGreen else if (isSelected) ErrorContainerCrimson else ChipBgGrey
                        } else {
                            if (isSelected) SecondaryPurple.copy(alpha = 0.15f) else ChipBgGrey
                        }
                        val borderCol = if (isSelected) SecondaryPurple else OutlineVariantBorder

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(containerBg)
                                .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                .clickable(enabled = !quizAnswered) {
                                    selectedAnswerIndex = idx
                                }
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = if (idx == 1 && quizAnswered) "✓" else if (isSelected && quizAnswered) "✗" else "${'A' + idx}.",
                                    fontWeight = FontWeight.Bold,
                                    color = if (idx == 1 && quizAnswered) SuccessGreen else if (isSelected && quizAnswered) ErrorCrimson else SecondaryPurple,
                                    fontSize = 13.sp
                                )
                                Text(text = op, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (quizAnswered) {
                        Text(
                            text = if (selectedAnswerIndex == 1) "Correct! Dirichlet conditions require periodic f(x) to possess finite local extreme points and finite bounded step gaps." else "Incorrect. The correct answer is B. Dirichlet formulation requires finite discontinuities.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedAnswerIndex == 1) SuccessGreen else ErrorCrimson
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (!quizAnswered) {
                            Button(
                                onClick = { quizAnswered = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryPurple),
                                enabled = selectedAnswerIndex != -1,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("SUBMIT ANSWER")
                            }
                        } else {
                            Button(
                                onClick = { showQuizDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Great")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExplanationDialog) {
        Dialog(onDismissRequest = { showExplanationDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Mechanics", tint = SecondaryPurple)
                            Text(
                                text = "Laplace Transform Simplified",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showExplanationDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Think of Laplace Transform as a magical lens that converts tough differential equations into simple algebra! It maps any continuous time function f(t) into the complex frequency domain F(s) using:\n\n L{f(t)} = ∫ e^(-st) f(t) dt from 0 to ∞.\n\nOnce in the s-domain, instead of solving calculus, we solve simple algebra fractions, then transform back to get our mechanical answer instantly!",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { showExplanationDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Let's Try solving!")
                    }
                }
            }
        }
    }
}

@Composable
fun FocusItemRow(
    title: String,
    detail: String,
    badgeText: String,
    badgeColor: Color,
    badgeBgColor: Color,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(2.dp, SecondaryPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(SecondaryPurple, CircleShape)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Schedule clock icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = detail,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = badgeText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = badgeColor,
            modifier = Modifier
                .background(badgeBgColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ChipBgGrey),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = SecondaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// 2. SYLLABUS UPLOAD / PARSING SCREEN
// ==========================================
@Composable
fun SyllabusScreen(viewModel: AppViewModel) {
    val syllabusInput by viewModel.syllabusInput.collectAsState()
    val isProcessing by viewModel.isSyllabusProcessing.collectAsState()
    val progressValue by viewModel.syllabusProgressValue.collectAsState()
    val syllabusList by viewModel.syllabusModules.collectAsState()

    var activePresetText by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Initialize Your Semester",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Upload or paste your BTech syllabus. Our AI agent will identify primary milestones, core weightage topics, and code tasks.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Simulative Drag & Drop field
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast
                            .makeText(
                                context,
                                "Simulated PDF selection! Use text area below to customize parameters.",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                    .border(
                        width = 1.5.dp,
                        color = SecondaryPurple.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                color = BackgroundSurface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SecondaryPurple.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload Arrow Logo",
                            tint = SecondaryPurple,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "Drop your PDF syllabus here",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Maximum file size: 20MB. PDF format preferred for academic structure.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            Toast.makeText(context, "File browser opened.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("BROWSE FILES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Raw Manual TextBox layout for students
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Clipboard icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Or paste syllabus text",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Presets load buttons to avoid user typing effort
                        OutlinedButton(
                            onClick = {
                                activePresetText = "BTech CSE Semester 6: Infinite Series, Fourier Transforms (Dirichlet, Euler harmonic analysis), Balanced Search Trees (Red Black Tree, dynamic rotations)."
                                viewModel.onSyllabusInputChanged(activePresetText)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("CSE Presets", fontSize = 10.sp, color = SecondaryPurple)
                        }
                    }

                    OutlinedTextField(
                        value = syllabusInput,
                        onValueChange = { viewModel.onSyllabusInputChanged(it) },
                        placeholder = {
                            Text(
                                "Copy and paste course objectives, modules, and credit info directly from your portal...",
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("syllabus_text_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryPurple,
                            unfocusedBorderColor = OutlineVariantBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isProcessing) "Parsing with Gemini..." else "Ready to analyze",
                            fontSize = 12.sp,
                            color = if (isProcessing) SecondaryPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isProcessing) FontWeight.Bold else FontWeight.Normal
                        )

                        Button(
                            onClick = { viewModel.processSyllabusText() },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryPurple),
                            shape = RoundedCornerShape(8.dp),
                            enabled = syllabusInput.isNotBlank() && !isProcessing,
                            modifier = Modifier.testTag("process_syllabus_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoFixHigh,
                                        contentDescription = "Magic wand Sparkle",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text("PROCESS WITH AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Live Processing Analysis Status Sidebar widget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .matchParentSize()
                            .background(SecondaryPurple)
                    )

                    Column(
                        modifier = Modifier.padding(start = 21.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Analysis Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Status Item 1: Complete
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Check",
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                            Column {
                                Text("User Authentication", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("BTech ID Verified", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Status Item 2: Processing
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = SecondaryPurple,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(SuccessContainerGreen, CircleShape)
                                        .border(2.dp, SuccessGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Success check icon",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                            Column {
                                Text("OCR & Parsing", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (isProcessing) "Extracting modules..." else "Extraction Complete",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Status Item 3: Roadmap
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val waitingColor = if (syllabusList.isNotEmpty()) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(
                                        width = 1.dp,
                                        color = waitingColor,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (syllabusList.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(SuccessGreen, CircleShape)
                                    )
                                }
                            }
                            Column {
                                Text("Roadmap Generation", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (syllabusList.isNotEmpty()) "Roadmap generated!" else "Waiting for data...",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Progress Bar Slider representation
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("OVERALL PROGRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$progressValue%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryPurple)
                            }

                            val animatedWidth by animateFloatAsState(targetValue = progressValue / 100f, label = "loading_prog")
                            LinearProgressIndicator(
                                progress = { animatedWidth },
                                color = SecondaryPurple,
                                trackColor = ChipBgGrey,
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Parsing Tips Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ChipBgGrey)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Bulb",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Parsing Tips",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    listOf(
                        "Ensure text is clear and not blurry or skewed in scanned copies.",
                        "Include the \"Course Outcomes\" section for better goal setting.",
                        "PDFs from official university portals yield the highest accuracy."
                    ).forEach { tip ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Star check badge icon",
                                tint = SecondaryPurple,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Text(text = tip, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. TASKS / DAILY FOCUS SCHEDULE SCREEN
// ==========================================
@Composable
fun TasksScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val studyTasks by viewModel.studyTasks.collectAsState()
    val isOverwhelmed by viewModel.isOverwhelmed.collectAsState()

    var activeTaskTipDialogText by remember { mutableStateOf<String?>(null) }

    // Filter tasks if student has acknowledged feeling overwhelmed
    val displayTasks = if (isOverwhelmed) {
        studyTasks.take(2) // Reduced/Compact Study schedule as per mandate
    } else {
        studyTasks
    }

    val unfinishedCount = displayTasks.count { !it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Daily Focus",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryPurple,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "You have $unfinishedCount tasks left.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Overwhelmed Action button
                    TextButton(
                        onClick = { viewModel.setOverwhelmed(!isOverwhelmed) },
                        modifier = Modifier.testTag("toggle_overwhelmed_button")
                    ) {
                        Text(
                            text = if (isOverwhelmed) "Normal View" else "Overwhelmed?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isOverwhelmed) SuccessGreen else ErrorCrimson
                        )
                    }
                }
            }
        }

        // Overwhelmed customized advice panel
        if (isOverwhelmed) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessContainerGreen),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.SentimentSatisfiedAlt, contentDescription = "Frown-Satisfied icon", tint = SuccessGreen)
                        Column {
                            Text(
                                text = "Take a breath. Shorter schedule generated!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SuccessGreen
                            )
                            Text(
                                "We've focused down to the top 2 urgent items. Study for 25 mins, then take a mandatory 15-min walk.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Main scheduler progress bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = "Graph icon", tint = SecondaryPurple)
                        Text(
                            text = "Core Daily Study Completion Rate",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val denominator = if (displayTasks.isEmpty()) 1 else displayTasks.size
                    val fraction = (displayTasks.count { it.isCompleted }.toFloat() / denominator) * 100
                    Text(
                        text = "${fraction.toInt()}% Done",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryPurple
                    )
                }
            }
        }

        // List of Study Tasks
        items(displayTasks) { task ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (task.isCompleted) BackgroundSurface else CardSurfaceWhite
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Left lateral accent line
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .matchParentSize()
                            .background(if (task.isUrgent) ErrorCrimson else SecondaryPurple)
                    )

                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = task.categoryTag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .background(ChipBgGrey, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )

                                if (task.isUrgent) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PriorityHigh,
                                            contentDescription = "Urgent Notice Alert",
                                            tint = ErrorCrimson,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "Urgent",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ErrorCrimson
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Minutes",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${task.estimatedMinutes} mins",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = task.topic,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )

                        Text(
                            text = task.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.toggleTaskStatus(task) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (task.isCompleted) ChipBgGrey else PrimaryNavy
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("task_complete_btn_${task.id}"),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Default.Undo else Icons.Default.CheckCircle,
                                        contentDescription = "Finish icon status",
                                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (task.isCompleted) "Undo Status" else "Mark as Done",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.White
                                    )
                                }
                            }

                            // Resource tip spark button
                            OutlinedIconButton(
                                onClick = {
                                    activeTaskTipDialogText = task.tipOrResource.ifBlank { "Ask other class forums, YouTube, or college LMS library." }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, OutlineVariantBorder, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = "Tips sparkle logo",
                                    tint = SecondaryPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Study Optimization Tips Block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryNavy)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OfflineBolt,
                            contentDescription = "Fast Power Bolt",
                            tint = SecondaryPurple,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Study Optimization",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Based on your current speed, you should start with \"Fourier Transform\" while your cognitive energy is highest this morning.",
                            fontSize = 12.sp,
                            color = OnSecondaryContainerPurple,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }

    if (activeTaskTipDialogText != null) {
        Dialog(onDismissRequest = { activeTaskTipDialogText = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Bulb", tint = AcademicGold)
                            Text(
                                text = "Resource Tip / Suggestions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { activeTaskTipDialogText = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close suggestions")
                        }
                    }
                    Text(
                        text = activeTaskTipDialogText ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                    Button(
                        onClick = { activeTaskTipDialogText = null },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Understood")
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. GROUPS / WHATSAPP MONITOR SCREEN
// ==========================================
@Composable
fun GroupsScreen(viewModel: AppViewModel) {
    val whatsappInput by viewModel.whatsappInput.collectAsState()
    val isProcessing by viewModel.isWhatsAppProcessing.collectAsState()
    val announcements by viewModel.whatsappAnnouncements.collectAsState()
    val extractedTasks by viewModel.extractedTasks.collectAsState()
    val chatMessages by viewModel.whatsappMessages.collectAsState()

    var customPastePreset by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Heading with Stats badges
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "INTELLIGENT MONITORING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryPurple,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "WhatsApp Group Pulse",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "AI is analyzing messages from 14 active academic groups to keep you updated on what matters most.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Status indicators
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(ChipBgGrey, RoundedCornerShape(8.dp))
                            .border(0.5.dp, SecondaryPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Flag, contentDescription = "Flags", tint = SecondaryPurple, modifier = Modifier.size(14.dp))
                            Text(text = "3 New Flags", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(ChipBgGrey, RoundedCornerShape(8.dp))
                            .border(0.5.dp, SecondaryPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = "Tasks Count", tint = SecondaryPurple, modifier = Modifier.size(14.dp))
                            Text(text = "2 Tasks Identified", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // WhatsApp group pasting box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Extract Class WhatsApp Notices",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedButton(
                            onClick = {
                                customPastePreset = "BTech Group: Suresh says 'Hey has anyone noted down microprocessor reference books? Prof mentioned Gaonkar and Douglas in class.' Admin alert: 'End Sem exams schedule released! Major milestone starts May 15. VIEW PDF link.'"
                                viewModel.onWhatsAppInputChanged(customPastePreset)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Mock Chat Feed", fontSize = 10.sp, color = SecondaryPurple)
                        }
                    }

                    OutlinedTextField(
                        value = whatsappInput,
                        onValueChange = { viewModel.onWhatsAppInputChanged(it) },
                        placeholder = {
                            Text("Paste student groups, announcements, or messages conversation text to analyze...", fontSize = 13.sp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("whatsapp_paste_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryPurple,
                            unfocusedBorderColor = OutlineVariantBorder
                        )
                    )

                    Button(
                        onClick = { viewModel.processWhatsAppMessages() },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryPurple),
                        shape = RoundedCornerShape(8.dp),
                        enabled = whatsappInput.isNotBlank() && !isProcessing,
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("process_whatsapp_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Analytics, contentDescription = "Wand", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Text("EXTRACT ANNOUNCEMENTS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Important Announcements Feed
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Important Announcements",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TextButton(onClick = {
                        Toast.makeText(context, "All marked as read.", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("MARK ALL READ", fontSize = 11.sp, color = SecondaryPurple, fontWeight = FontWeight.Bold)
                    }
                }

                if (announcements.isEmpty()) {
                    Text("No announcements found yet.", fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    announcements.forEach { announce ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .matchParentSize()
                                        .background(if (announce.isUrgent) ErrorCrimson else SecondaryPurple)
                                )

                                Column(
                                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = announce.groupName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .background(ChipBgGrey, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                        Text(
                                            text = announce.timeString,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = announce.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        text = announce.body,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )

                                    if (announce.fileUrl != null || announce.isUrgent) {
                                        Row(
                                            modifier = Modifier.padding(top = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            if (announce.fileUrl != null) {
                                                OutlinedButton(
                                                    onClick = {
                                                        Toast.makeText(context, "Opening PDF: ${announce.fileUrl}", Toast.LENGTH_SHORT).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Attachment, contentDescription = "PDF link", modifier = Modifier.size(12.dp))
                                                        Text("VIEW PDF", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }

                                            TextButton(
                                                onClick = {
                                                    Toast.makeText(context, "Calendar Sync initialized!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.height(28.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Schedule", tint = SecondaryPurple, modifier = Modifier.size(12.dp))
                                                    Text("ADD TO CALENDAR", fontSize = 10.sp, color = SecondaryPurple, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: AI Extracted Tasks Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ChipBgGrey),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OfflineBolt, contentDescription = "Spark", tint = SecondaryPurple)
                        Text(
                            text = "AI Extracted Tasks",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (extractedTasks.isEmpty()) {
                        Text("No tasks extracted yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        extractedTasks.forEach { extT ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(0.5.dp, OutlineVariantBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = extT.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Source: ${extT.source}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "Alert",
                                            tint = ErrorCrimson,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = extT.deadlineText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ErrorCrimson
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Tasks synced to Google Tasks successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SYNC TO GOOGLE TASKS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Core Dates milestones widget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryNavy)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Key Dates",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Divider(color = Color.White.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnSecondaryContainerPurple)
                                Text("15", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }

                        Column {
                            Text("End Sem Exams", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Major Session Milestone", fontSize = 10.sp, color = OnSecondaryContainerPurple)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("APR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnSecondaryContainerPurple)
                                Text("28", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }

                        Column {
                            Text("Final Project Review", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Internal PCB Lab Delta", fontSize = 10.sp, color = OnSecondaryContainerPurple)
                        }
                    }
                }
            }
        }

        // Chat pulse simulation (Chat bubbles matching Suresh A & AI assistant replies)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Message Pulse",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (chatMessages.isEmpty()) {
                    Text("Chat empty.", fontSize = 12.sp)
                } else {
                    chatMessages.forEach { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isFromAi) Arrangement.End else Arrangement.Start,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                if (!msg.isFromAi) {
                                    // SA initials circle
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(SecondaryPurple),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "SA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Face, contentDescription = "AI", tint = PrimaryNavy, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(text = msg.sender, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = msg.senderTag,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .background(ChipBgGrey, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Text(text = msg.timeString, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    val bubbleBg = if (msg.isFromAi) OnSecondaryContainerPurple.copy(alpha = 0.5f) else ChipBgGrey.copy(alpha = 0.5f)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(topStart = if (!msg.isFromAi) 0.dp else 12.dp, topEnd = if (msg.isFromAi) 0.dp else 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp))
                                            .background(bubbleBg)
                                            .border(
                                                width = 0.5.dp,
                                                color = if (msg.isFromAi) SecondaryPurple.copy(alpha = 0.3f) else OutlineVariantBorder,
                                                shape = RoundedCornerShape(topStart = if (!msg.isFromAi) 0.dp else 12.dp, topEnd = if (msg.isFromAi) 0.dp else 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = msg.text,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            if (msg.isFromAi) {
                                                TextButton(
                                                    onClick = {
                                                        Toast.makeText(context, "Advice shared back to Section A Group!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                                    modifier = Modifier
                                                        .padding(top = 6.dp)
                                                        .height(28.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(12.dp), tint = SecondaryPurple)
                                                        Text("POST TO GROUP", fontSize = 11.sp, color = SecondaryPurple, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. PROFILE / EDIT SEMESTER CONTEXT SCREEN
// ==========================================
@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    val studentProfile by viewModel.studentProfile.collectAsState()
    val context = LocalContext.current

    var editName by remember { mutableStateOf("") }
    var editBranch by remember { mutableStateOf("") }
    var editSem by remember { mutableStateOf(6) }

    LaunchedEffect(studentProfile) {
        studentProfile?.let {
            editName = it.name
            editBranch = it.branch
            editSem = it.semester
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Student Profile & Context",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Configure student branch, current semester and active presets. AI dynamically shapes study objectives and theory details according to these params.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Edit Core Identity Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Student Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Branch Selection list: CSE, CSM, CAI
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Branch Focus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("CSE", "CSM", "CAI").forEach { b ->
                                val isSelected = editBranch.uppercase() == b
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SecondaryPurple else ChipBgGrey)
                                        .clickable { editBranch = b }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = b,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Semester Selector slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Semester Level", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Current: Sem $editSem", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecondaryPurple)
                        }
                        Slider(
                            value = editSem.toFloat(),
                            onValueChange = { editSem = it.toInt() },
                            valueRange = 1f..8f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = SecondaryPurple,
                                activeTrackColor = SecondaryPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.saveStudentProfile(editName, editBranch, editSem)
                            Toast.makeText(context, "Academic profile saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE CHANGES", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ChipBgGrey),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Reset Demonstrative Seed Data",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorCrimson
                    )
                    Text(
                        text = "Resetting clears any uploaded syllabus and returns database storage to its initial, beautiful demonstrative state showing matching math/CS modules, WhatsApp announcements and timeline schedules.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.resetData()
                            Toast.makeText(context, "Demonstrative default data reloaded!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorCrimson),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.linearGradient(listOf(ErrorCrimson, ErrorCrimson))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RESET AND RESTORE DEMO TEXT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
