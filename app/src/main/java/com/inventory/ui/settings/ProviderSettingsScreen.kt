package com.inventory.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsScreen(
    providerType: SyncProviderType,
    onBack: () -> Unit,
    viewModel: SyncSettingsViewModel = hiltViewModel()
) {
    val initial = remember(providerType) { viewModel.getProviderSettings(providerType) }
    var settings by rememberSaveable { mutableStateOf(initial) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(providerType.displayName, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Формат файлу — для всіх провайдерів
            FormatDropdown(
                selected = settings.format,
                onSelect = { settings = settings.copy(format = it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Поля специфічні для провайдера
            when (providerType) {
                SyncProviderType.LOCAL_FOLDER -> LocalFolderFields(settings) { settings = it }
                SyncProviderType.HTTP_API -> HttpApiFields(settings) { settings = it }
                SyncProviderType.FTP -> FtpFields(settings) { settings = it }
                SyncProviderType.WEBDAV -> WebDavFields(settings) { settings = it }
                SyncProviderType.EMAIL -> EmailFields(settings) { settings = it }
                SyncProviderType.TELEGRAM -> TelegramFields(settings) { settings = it }
                SyncProviderType.ONE_C -> OneCFields(settings) { settings = it }
                SyncProviderType.ONEDRIVE -> StubInfo("OneDrive буде доступний у наступній версії")
                SyncProviderType.GOOGLE_DRIVE -> StubInfo("Google Drive буде доступний у наступній версії")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveProviderSettings(settings)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Зберегти", fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormatDropdown(selected: SyncFormat, onSelect: (SyncFormat) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Формат файлу") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SyncFormat.entries.forEach { format ->
                DropdownMenuItem(
                    text = { Text(format.displayName) },
                    onClick = { onSelect(format); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun LocalFolderFields(s: SyncSettings, onChange: (SyncSettings) -> Unit) {
    FieldText("Шлях до папки", s.path) { onChange(s.copy(path = it)) }
}

@Composable
private fun HttpApiFields(s: SyncSettings, onChange: (SyncSettings) -> Unit) {
    FieldText("URL сервера", s.apiUrl) { onChange(s.copy(apiUrl = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("API токен", s.apiToken, password = true) { onChange(s.copy(apiToken = it)) }
}

@Composable
private fun FtpFields(s: SyncSettings, onChange: (SyncSettings) -> Unit) {
    FieldText("Хост", s.host) { onChange(s.copy(host = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldNumber("Порт (за замовч. 21)", s.port) { onChange(s.copy(port = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Логін", s.username) { onChange(s.copy(username = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Пароль", s.password, password = true) { onChange(s.copy(password = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Шлях на сервері", s.path) { onChange(s.copy(path = it)) }
}

@Composable
private fun WebDavFields(s: SyncSettings, onChange: (SyncSettings) -> Unit) {
    FieldText("WebDAV URL", s.webDavUrl) { onChange(s.copy(webDavUrl = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Логін", s.username) { onChange(s.copy(username = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Пароль", s.password, password = true) { onChange(s.copy(password = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Шлях (папка)", s.path) { onChange(s.copy(path = it)) }
}

@Composable
private fun EmailFields(s: SyncSettings, onChange: (SyncSettings) -> Unit) {
    FieldText("SMTP сервер", s.smtpHost) { onChange(s.copy(smtpHost = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldNumber("SMTP порт (587)", s.smtpPort) { onChange(s.copy(smtpPort = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Від (email)", s.emailFrom) { onChange(s.copy(emailFrom = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Кому (email)", s.emailTo) { onChange(s.copy(emailTo = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Логін", s.username) { onChange(s.copy(username = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Пароль", s.password, password = true) { onChange(s.copy(password = it)) }
}

@Composable
private fun TelegramFields(s: SyncSettings, onChange: (SyncSettings) -> Unit) {
    FieldText("Bot Token", s.telegramBotToken) { onChange(s.copy(telegramBotToken = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Chat ID", s.telegramChatId) { onChange(s.copy(telegramChatId = it)) }
}

@Composable
private fun OneCFields(s: SyncSettings, onChange: (SyncSettings) -> Unit) {
    FieldText("URL 1C", s.oneCUrl) { onChange(s.copy(oneCUrl = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Логін", s.oneCLogin) { onChange(s.copy(oneCLogin = it)) }
    Spacer(modifier = Modifier.height(8.dp))
    FieldText("Пароль", s.oneCPassword, password = true) { onChange(s.copy(oneCPassword = it)) }
}

@Composable
private fun StubInfo(text: String) {
    Text(text = text, color = androidx.compose.material3.MaterialTheme.colorScheme.outline)
}

@Composable
private fun FieldText(
    label: String,
    value: String,
    password: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@Composable
private fun FieldNumber(label: String, value: Int, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { onChange(it.toIntOrNull() ?: 0) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
