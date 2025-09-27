package com.yourname.expenso.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourname.expenso.data.AppTheme
import com.yourname.expenso.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.theme.collectAsState(initial = AppTheme.SYSTEM)
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(horizontal = if (isTablet) 32.dp else 0.dp)
        ) {
            item { ProfileSection() }
            
            item { SettingsSectionHeader("💰 Financial Management") }
            item { SettingsActionItem("📊 Manage Categories") { navController.navigate("manage_categories") } }
            item { SettingsActionItem("🏦 Manage Accounts") { navController.navigate("manage_accounts") } }



            
            item { SettingsSectionHeader("🎨 Appearance & Display") }
            item {
                ThemeSelectionSection(
                    currentTheme = currentTheme,
                    onThemeChanged = { viewModel.setTheme(it) }
                )
            }
            item { SettingsActionItem("💱 Currency: ₹ INR") { } }
            item { SettingsActionItem("📅 Date Format: DD/MM/YYYY") { } }
            item { SettingsActionItem("🔢 Number Format: 1,23,456.78") { } }
            

            
            item { SettingsSectionHeader("🗑️ More Options") }
            item { SettingsActionItem("🗋 Generate Report") { navController.navigate("reports") } }
            item { SettingsActionItem("🗜️ Recently Deleted") { navController.navigate("recently_deleted") } }

            item { SettingsActionItem("🔄 Reset App Data") { showResetDialog = true } }
            
            item { SettingsSectionHeader("ℹ️ About & Support") }
            item { SettingsActionItem("📱 App Version: v1.0.0") { } }
            item { SettingsActionItem("📞 Contact Support") { } }
            item { SettingsActionItem("⭐ Rate App") { } }
            item { SettingsActionItem("📄 Privacy Policy") { } }
        }
        

        
        if (showResetDialog) {
            ResetAppDataDialog(
                onConfirm = {
                    viewModel.resetAppData()
                    showResetDialog = false
                },
                onDismiss = { showResetDialog = false }
            )
        }
    }
}

@Composable
fun ProfileSection() {
    var showEditDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("John Doe") }
    var userEmail by remember { mutableStateOf("john.doe@example.com") }
    var selectedAvatar by remember { mutableStateOf(Icons.Default.Person) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.size(72.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            selectedAvatar,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(Modifier.width(20.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        userEmail,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Member since Jan 2024",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Edit Profile")
            }
        }
    }
    
    if (showEditDialog) {
        ModernEditProfileDialog(
            currentName = userName,
            currentEmail = userEmail,
            currentAvatar = selectedAvatar,
            onDismiss = { showEditDialog = false },
            onSave = { name, email, avatar ->
                userName = name
                userEmail = email
                selectedAvatar = avatar
                showEditDialog = false
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp, 24.dp, 16.dp, 8.dp)
    )
}

@Composable
fun SettingsActionItem(title: String, onClick: () -> Unit) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ThemeSelectionSection(
    currentTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "Theme",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                AppTheme.LIGHT to "Light",
                AppTheme.DARK to "Dark",
                AppTheme.SYSTEM to "System"
            ).forEach { (theme, label) ->
                FilterChip(
                    onClick = { onThemeChanged(theme) },
                    label = { Text(label) },
                    selected = currentTheme == theme
                )
            }
        }
    }
}

@Composable
fun ModernEditProfileDialog(
    currentName: String,
    currentEmail: String,
    currentAvatar: androidx.compose.ui.graphics.vector.ImageVector,
    onDismiss: () -> Unit,
    onSave: (String, String, androidx.compose.ui.graphics.vector.ImageVector) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var selectedAvatar by remember { mutableStateOf(currentAvatar) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    
    val avatarOptions = listOf(
        Icons.Default.Person,
        Icons.Default.AccountCircle,
        Icons.Default.Face,
        Icons.Default.Star,
        Icons.Default.Favorite
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Edit Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Avatar Selection
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable { showPhotoOptions = !showPhotoOptions },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                selectedAvatar,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    TextButton(onClick = { showPhotoOptions = !showPhotoOptions }) {
                        Text("Change Avatar")
                    }
                }
                
                // Avatar Options
                if (showPhotoOptions) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Choose Avatar:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(Modifier.height(12.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                avatarOptions.forEach { avatar ->
                                    Card(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clickable { 
                                                selectedAvatar = avatar
                                                showPhotoOptions = false
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedAvatar == avatar)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                avatar,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = if (selectedAvatar == avatar)
                                                    MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                            

                        }
                    }
                }
                
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Email Field (Optional)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, email, selectedAvatar) },
                enabled = name.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}