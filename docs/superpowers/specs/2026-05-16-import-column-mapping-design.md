# Import Column Mapping — Design Spec

**Date:** 2026-05-16
**Branch / Worktree:** `claude/youthful-banach-06ee88`
**Status:** Draft, pending user review
**Scope:** New feature — interactive column mapping for catalog import (CSV/XLSX/JSON)

## 1. Problem

Поточний імпорт каталогу (`SyncEngine.runImport` → `SyncSerializer.deserialize` → `InventoryRepository.importItems`) припускає, що файл має **фіксовані канонічні заголовки**: `barcode`, `name`, `quantity`, `unit`, `description`. Якщо файл від постачальника:
- має інші назви колонок (наприклад, `артикул`, `найменування`, `залишок`), або
- не має взагалі рядка заголовків (тільки дані з першого рядка),

— імпорт «мовчки» втрачає всі рядки (бо `row["barcode"]` повертає `null`). Користувач не отримує ні мапінга, ні діагностики.

Потрібно дати користувачу інтерактивний крок: побачити прев'ю файлу, підтвердити чи рядок 0 — заголовки, і вручну/напівавтоматично прив'язати кожну колонку файлу до цільового поля каталогу.

## 2. Constraints

- Android-only, Jetpack Compose, Kotlin, Hilt, Room v3, WorkManager — як зараз.
- Архітектура offline-first з outbox-pattern має бути збережена: будь-яка зміна залишку проходить через `inventory_operations` + `outbox_entries` (atomic transaction `recordOperationWithOutbox`).
- Не ламати наявний автоматичний фоновий sync (`SyncWorker` → `SyncEngine.runImport`).
- Файли беруться лише з вже налаштованих import-провайдерів (LOCAL_FOLDER / FTP / SFTP / HTTP_API / WEBDAV). SAF picker — не використовувати.
- Дизайн має витримати майбутнє розширення схеми під TZ 8.2 (sku, додаткові штрихкоди, packageFlag, conversionFactor, status) — без переписування UI мапера.

## 3. Decisions (вирішено за час brainstorming)

| # | Питання | Рішення |
|---|---|---|
| 1 | Точка входу | Новий екран ручного імпорту, route `"import_catalog"`, кнопка з `ScanScreen` |
| 2 | Коли показувати мапінг | Завжди. Switch «Перший рядок містить назви колонок» з автодетектом за евристикою + автозаповнення мапінгу за fuzzy-збігом імен |
| 3 | Набір цільових полів | Динамічний реєстр `TargetField`. Початковий список: barcode*, name*, quantity, unit, description, min_quantity, notes, category (LOOKUP), location (LOOKUP). `*` = required |
| 4 | Стратегія upsert/quantity | ERP-aligned (підтверджено DeepWiki по Odoo та ERPNext): метадані upsert + per-row AUDIT-операція через `recordOperationWithOutbox`, якщо quantity змаплено. Категорії/локації — lookup-or-create за назвою |
| 5 | Джерело файлу | Список файлів через `SyncProvider.discoverImportFiles(format)` для кожного налаштованого import-провайдера |
| 6 | Збереження мапінгу | Запитуємо після успішного імпорту. Якщо так — у `SyncSettings.columnMapping` цього провайдера. Background sync читає його ж |
| 7 | Scope vs TZ замовника | Реалізуємо мапінг із поточною схемою. TZ 8.2 поля (sku, додаткові штрихкоди, ...) — окремі наступні задачі (див. `memory/project_post_mapping_backlog.md`) |

## 4. Architecture

```
ui/import/  (НОВЕ)
├── ImportCatalogScreen.kt           Compose host, NavHost route "import_catalog"
├── ImportCatalogViewModel.kt        @HiltViewModel, StateFlow<ImportCatalogUiState>
└── ImportCatalogUiState.kt          sealed: PickProvider | PickFile | MapColumns | Confirm | Result

sync/import/  (НОВЕ)
├── TargetField.kt                   data class + TargetFields реєстр (object)
├── TargetType.kt                    enum: STRING, DOUBLE, LOOKUP_CATEGORY, LOOKUP_LOCATION
├── ColumnMapping.kt                 data class (треат header flag + Map<Int, String?>)
├── ImportPreview.kt                 data class (headerRow, sampleRows, detectedHasHeader, totalRowsEstimate)
├── ImportReport.kt                  data class (counts + skipReasons)
└── ColumnMappingHeuristic.kt        pure: detectHasHeader, fuzzySuggestMapping
                                     (fuzzy: lowercase + trim не-літер, точний збіг
                                     АБО Levenshtein ≤ 2 АБО одне містить інше)
                                     (detectHasHeader: рядок 0 не header якщо
                                      ≥ 50% клітинок парсяться як число)

sync/serializer/   (РОЗШИРЕННЯ)
├── SyncSerializer.kt                + parsePreview(), + parseRaw()
├── CsvSerializer.kt                 нові методи + Windows-1251 fallback
├── ExcelSerializer.kt               нові методи
└── JsonSerializer.kt                нові методи (keys-as-header)

data/repository/  (РОЗШИРЕННЯ)
├── InventoryRepository.kt           + applyMappedImport(rawRows, mapping, targets): ImportReport
└── InventoryRepositoryImpl.kt       одна Room @Transaction: lookup-or-create FK, upsert метаданих, AUDIT через recordOperationWithOutbox

sync/  (МІНІМАЛЬНІ ЗМІНИ)
├── SyncSettings.kt                  + val columnMapping: ColumnMapping? = null
└── SyncEngine.kt                    applyImport(): якщо settings.columnMapping != null → parseRaw + applyMappedImport, інакше — стара логіка

app/MainActivity.kt                  + composable("import_catalog") { ImportCatalogScreen(...) }
ui/scan/ScanScreen.kt                + кнопка «Імпорт каталогу» → navigate("import_catalog")
```

## 5. User flow

```
Tap "Імпорт каталогу" в ScanScreen
    ↓
[1] PickProvider — список активних import-провайдерів
    ↓ (tap провайдера)
[2] PickFile — provider.discoverImportFiles(format) → список з ім'ям/розміром
    ↓ (tap файлу)
[3] MapColumns
    │   • provider.import(format, fileName) → bytes
    │   • serializer.parsePreview(bytes) → ImportPreview
    │   • Switch «Перший рядок містить заголовки» (default = ImportPreview.detectedHasHeader)
    │   • Таблиця: для кожної колонки файлу — Dropdown(TargetField або "Не імпортувати")
    │   • Required-поля підсвічуються червоним якщо не змаплені
    │   • Sample (5–10 рядків) під кожною колонкою для контексту
    │   • Кнопка "Далі" — disabled поки required не змаплені
    ↓ (tap "Далі")
[4] Confirm — підсумок з очікуваними counts + чек-боксом «Зберегти мапінг»
    ↓ (tap "Імпортувати")
[5] Result
    │   • Прогрес-бар під час applyMappedImport
    │   • Після: ImportReport з counts → toast/snackbar
    │   • Якщо чек-бокс активний → settingsManager.update(settings.copy(columnMapping = ...))
    ↓
Кнопки "Готово" / "Імпортувати ще файл"
```

## 6. Data contracts

### TargetField реєстр (`com.inventory.sync.import.TargetFields`)

```kotlin
object TargetFields {
    val BARCODE = TargetField("barcode", "Штрихкод", TargetType.STRING, isRequired = true)
    val NAME = TargetField("name", "Назва", TargetType.STRING, isRequired = true)
    val QUANTITY = TargetField("quantity", "Кількість", TargetType.DOUBLE)
    val UNIT = TargetField("unit", "Одиниця", TargetType.STRING)
    val DESCRIPTION = TargetField("description", "Опис", TargetType.STRING)
    val MIN_QUANTITY = TargetField("min_quantity", "Мін. залишок", TargetType.DOUBLE)
    val NOTES = TargetField("notes", "Примітки", TargetType.STRING)
    val CATEGORY = TargetField("category", "Категорія", TargetType.LOOKUP_CATEGORY)
    val LOCATION = TargetField("location", "Локація", TargetType.LOOKUP_LOCATION)

    val all: List<TargetField> = listOf(BARCODE, NAME, QUANTITY, UNIT, DESCRIPTION, MIN_QUANTITY, NOTES, CATEGORY, LOCATION)
    fun byId(id: String): TargetField? = all.firstOrNull { it.id == id }
}
```

### ColumnMapping

```kotlin
data class ColumnMapping(
    val treatFirstRowAsHeader: Boolean,
    val mapping: Map<Int, String?>   // file column index → TargetField.id (null = "Не імпортувати")
) : Serializable
```

### Repository signature

```kotlin
suspend fun applyMappedImport(
    rawRows: List<List<String?>>,
    mapping: ColumnMapping,
    targetFields: List<TargetField>
): ImportReport
```

Алгоритм (всередині `db.withTransaction`). `rawRows` приходять уже **без рядка заголовків** — це обов'язок `SyncSerializer.parseRaw`, який пропускає рядок 0, якщо `treatFirstRowAsHeader=true`.

1. Зібрати унікальні назви категорій/локацій з тих колонок, що мапляться у LOOKUP. Один lookup-or-create запит на кожну унікальну назву → map назва→id.
2. Для кожного рядка:
   - Зібрати `Map<TargetField.id, String?>` за `mapping`.
   - Якщо barcode або name порожні → skipCount++, продовжити.
   - `getByBarcode(barcode)`. Якщо існує — `update` з усіма метаданими (без quantity). Якщо ні — `insert` нового з quantity=0.
   - Якщо QUANTITY змаплено і значення парситься як Double → `recordOperationWithOutbox(AUDIT, qty, itemId)` через існуючий method.
3. Повернути `ImportReport`.

### SyncSerializer розширення

```kotlin
interface SyncSerializer {
    fun parsePreview(data: ByteArray, sampleSize: Int = 10): ImportPreview
    fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>>
}
```

- **CSV:** прочитати всі рядки, повернути перший як `headerRow` + наступні як `sampleRows[0..sampleSize-1]`. Кодування: спершу UTF-8; якщо у декодованому тексті перших 1 КБ є хоча б один `U+FFFD` (Unicode replacement character) або частка `U+FFFD` ≥ 1% — повторно декодувати як `windows-1251`. Це eвристика, не BOM-сніф (українські CSV з 1С часто без BOM).
- **Excel:** перший рядок sheet → headerRow, наступні → sampleRows.
- **JSON:** ключі першого об'єкта → headerRow, значення наступних об'єктів у порядку тих самих ключів → sampleRows. `treatFirstRowAsHeader` ігнорується (JSON має ключі за визначенням).

## 7. Edge cases

| Сценарій | Поведінка |
|---|---|
| Required-поле не змаплено | Кнопка «Далі» disabled, червоний highlight |
| Рядок без barcode | skipReasons[i] = "empty barcode" |
| Рядок без name | skipReasons[i] = "empty name" |
| Quantity не парситься як Double | Залишити qty=0, AUDIT не генерувати |
| Категорія/локація — порожня | NULL у FK |
| Файл порожній / corrupt | Step 3 показує помилку + Назад |
| CSV не UTF-8 | fallback Windows-1251 |
| 50k рядків | IO-coroutine + прогрес-бар |
| Помилка в applyMappedImport | Rollback транзакції, state=Failure |

## 8. Testing

- Unit (без Android): `ColumnMappingHeuristicTest`, `CsvSerializer.parsePreview/parseRaw`, `ExcelSerializer.parsePreview/parseRaw`, `JsonSerializer.parsePreview/parseRaw`.
- Repository (in-memory Room): `InventoryRepositoryImpl.applyMappedImport` — happy path, skip-логіка, AUDIT-генерація, lookup-or-create.
- ViewModel: `ImportCatalogViewModelTest` — переходи стейтів, валідація required, autosuggest.
- AndroidTest happy-path: LOCAL_FOLDER → test.csv UTF-8 → mapping → import → assert DB + outbox.

## 9. Out of scope (на цей спринт)

- Розширення схеми каталогу під TZ 8.2 (sku, додаткові штрихкоди, packageFlag, conversionFactor, status) — окремий спринт.
- BAF-протокол (потребує дослідження).
- SAF picker для імпорту з довільного місця на пристрої.
- Bulk-edit мапінгу (drag-rearrange колонок).
- Імпорт зображень товарів.
- Підтримка XML-формату.

## 10. Open items

Жодних. Усі ключові рішення прийняті за час brainstorming.
