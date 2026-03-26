# Architecture Documentation

## 📐 System Overview

Inventory APK is a native Android application designed with a clean, modular architecture that prioritizes offline-first functionality, maintainability, and scalability. The application follows modern Android development best practices, leveraging the Android Jetpack suite to provide a robust, testable, and user-friendly inventory management solution.

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Presentation Layer                   │
│              (Jetpack Compose UI + ViewModels)          │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                      Domain Layer                        │
│              (Use Cases + Business Logic)                │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                       Data Layer                         │
│         (Repositories + Room Database + APIs)            │
└─────────────────────────────────────────────────────────┘
```

### Core Principles

1. **Offline-First**: All core functionality works without internet connectivity
2. **Separation of Concerns**: Clear boundaries between UI, business logic, and data
3. **Single Source of Truth**: Room database as the authoritative data source
4. **Unidirectional Data Flow**: Data flows from data layer → domain → presentation
5. **Testability**: Architecture enables comprehensive unit and integration testing
6. **Reactive Programming**: UI updates automatically when data changes

---

## 🏛️ MVVM Architecture Pattern

### Overview

Inventory APK implements the **Model-View-ViewModel (MVVM)** architecture pattern, which provides clear separation between business logic and UI code, facilitating maintainability and testability.

### Component Breakdown

#### **Model**
- **Definition**: Represents the data layer and business logic
- **Components**:
  - Room database entities
  - Repository implementations
  - Domain models
  - Data sources (local database, future API services)
- **Responsibilities**:
  - Data persistence and retrieval
  - Business rule enforcement
  - Data transformation and validation

#### **View**
- **Definition**: UI layer responsible for displaying data and capturing user input
- **Components**:
  - Jetpack Compose composables
  - Activity/Fragment containers (minimal usage)
  - UI state representations
- **Responsibilities**:
  - Render UI based on ViewModel state
  - Capture user interactions
  - Navigate between screens
  - Display loading, error, and success states

#### **ViewModel**
- **Definition**: Mediator between View and Model
- **Components**:
  - AndroidX ViewModel classes
  - UI state holders
  - Use case orchestrators
- **Responsibilities**:
  - Expose UI state to the View via StateFlow/LiveData
  - Handle user actions and delegate to Use Cases
  - Manage UI-related data lifecycle
  - Survive configuration changes
  - No Android framework dependencies (except AndroidX ViewModel)

### Data Flow Example

```
User Action (e.g., "Add Item")
        │
        ▼
    Composable (View)
        │
        ▼
    ViewModel.onAddItemClicked()
        │
        ▼
    AddItemUseCase.execute()
        │
        ▼
    ItemRepository.insertItem()
        │
        ▼
    Room Database (Model)
        │
        ▼
    Flow<List<Item>> emits updated data
        │
        ▼
    ViewModel updates UI State
        │
        ▼
    Composable re-renders with new data
```

### Benefits of MVVM for Inventory APK

- **Testability**: ViewModels can be unit tested without Android framework
- **Lifecycle Awareness**: ViewModels survive configuration changes
- **Reactive UI**: Flow/StateFlow ensures UI always reflects current data state
- **Maintainability**: Clear separation makes code easier to modify and extend
- **Scalability**: Easy to add new features without impacting existing code

---

## 🛠️ Technology Stack

### Language & Framework

#### **Kotlin** (Primary Language)
- **Why**: Modern, concise, null-safe, fully interoperable with Java
- **Benefits**:
  - Coroutines for asynchronous programming
  - Extension functions for cleaner code
  - Data classes for model definitions
  - Sealed classes for state management
  - Strong type system reduces bugs

#### **Minimum SDK: API 24 (Android 7.0 Nougat)**
- **Rationale**:
  - Covers ~95% of active Android devices (as of 2026)
  - Supports modern Jetpack libraries
  - Reasonable baseline for features like multi-window support
- **Target SDK: Latest stable Android version**

### UI Framework

#### **Jetpack Compose**
- **Why**: Modern declarative UI toolkit
- **Benefits**:
  - Less boilerplate than XML layouts
  - Reactive UI that responds to state changes
  - Built-in Material Design 3 components
  - Easier to build complex, animated UIs
  - Better preview tools
  - Type-safe navigation

#### **Material Design 3**
- **Why**: Consistent, accessible, modern design system
- **Benefits**:
  - Pre-built components (cards, lists, buttons, etc.)
  - Dynamic color theming
  - Accessibility built-in
  - Professional appearance

### Database & Persistence

#### **Room Persistence Library**
- **Why**: Official Android ORM, compile-time SQL verification
- **Benefits**:
  - Type-safe database access
  - Compile-time query verification (catches SQL errors early)
  - LiveData/Flow integration for reactive queries
  - Migration support for schema changes
  - Built on SQLite for reliability

#### **SQLite**
- **Why**: Battle-tested, embedded database
- **Benefits**:
  - No server required (offline-first)
  - ACID compliance for data integrity
  - Efficient for mobile data volumes
  - Zero-configuration
  - Small footprint

### Dependency Injection

#### **Hilt** (Dagger for Android)
- **Why**: Official DI framework for Android
- **Benefits**:
  - Compile-time dependency graph validation
  - Reduces boilerplate with code generation
  - Lifecycle-aware scopes
  - Standard architecture components integration
  - Easier testing with test doubles

**Alternative Considered**: Koin (runtime DI)
- **Decision**: Chose Hilt for compile-time safety and official Google support

### Asynchronous Programming

#### **Kotlin Coroutines**
- **Why**: First-class Kotlin support for async operations
- **Benefits**:
  - Sequential async code (no callback hell)
  - Structured concurrency (automatic cancellation)
  - Exception handling built-in
  - Lightweight threads (can run thousands)
  - Easy to test

#### **Kotlin Flow**
- **Why**: Reactive streams for Kotlin coroutines
- **Benefits**:
  - Cold streams (only active when collected)
  - Backpressure handling
  - Operator chaining (map, filter, etc.)
  - StateFlow/SharedFlow for state management
  - Lifecycle-aware collection in UI

### Additional Libraries (Planned)

#### **Barcode Scanning**
- **Option 1**: ML Kit Barcode Scanning
  - On-device processing
  - No internet required
  - Free
- **Option 2**: ZXing
  - Open source
  - Mature, well-tested
  - More customization options

**Decision**: Evaluate both during implementation; ML Kit preferred for seamless integration

#### **Data Export**
- **Apache POI** or **OpenCSV**: For Excel/CSV export functionality

#### **Camera Integration**
- **CameraX**: Jetpack library for camera access (barcode scanning, item photos)

---

## 💾 Data Layer Architecture

### Overview

The data layer is the single source of truth for all application data. It implements the repository pattern to abstract data sources from the rest of the application.

### Components

#### **1. Database (Room)**

**Entities**: Define database schema
```kotlin
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "sku") val sku: String,
    @ColumnInfo(name = "quantity") val quantity: Int,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
```

**DAOs (Data Access Objects)**: Define database operations
```kotlin
@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)
}
```

**Database Class**: Singleton database instance
```kotlin
@Database(entities = [ItemEntity::class], version = 1)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
```

#### **2. Repositories**

**Purpose**: Abstract data sources and provide clean API to domain layer

```kotlin
interface ItemRepository {
    fun getAllItems(): Flow<List<Item>>
    suspend fun getItemById(id: Long): Item?
    suspend fun insertItem(item: Item): Long
    suspend fun updateItem(item: Item)
    suspend fun deleteItem(item: Item)
    suspend fun searchItems(query: String): List<Item>
}

class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao
) : ItemRepository {
    override fun getAllItems(): Flow<List<Item>> {
        return itemDao.getAllItems().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    // ... implementation
}
```

#### **3. Data Mappers**

**Purpose**: Convert between database entities and domain models

```kotlin
// Entity → Domain Model
fun ItemEntity.toDomainModel(): Item {
    return Item(
        id = id,
        name = name,
        sku = sku,
        quantity = quantity,
        category = category,
        createdAt = Instant.ofEpochMilli(createdAt)
    )
}

// Domain Model → Entity
fun Item.toEntity(): ItemEntity {
    return ItemEntity(
        id = id,
        name = name,
        sku = sku,
        quantity = quantity,
        category = category,
        createdAt = createdAt.toEpochMilli()
    )
}
```

### Database Schema Design

**Initial Tables** (MVP):
- `items`: Core inventory items
- `categories`: Item categories (future)
- `stock_movements`: History of quantity changes (future)

**Design Principles**:
- Normalized schema to reduce redundancy
- Foreign key constraints for referential integrity
- Indexed columns for fast queries (SKU, name)
- Timestamps for audit trails

### Future Enhancements
- Cloud sync capability (Room → Remote API)
- Conflict resolution for multi-device sync
- DataStore for app preferences
- Encrypted database for sensitive data

---

## 🎨 UI Layer Architecture

### Jetpack Compose Structure

#### **Screen-Level Composables**

Each screen is a top-level composable function:

```kotlin
@Composable
fun ItemListScreen(
    viewModel: ItemListViewModel = hiltViewModel(),
    onItemClick: (Item) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ItemListContent(
        uiState = uiState,
        onItemClick = onItemClick,
        onAddClick = { viewModel.onAddItemClicked() }
    )
}
```

#### **Content Composables**

Stateless UI components:

```kotlin
@Composable
fun ItemListContent(
    uiState: ItemListUiState,
    onItemClick: (Item) -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold(
        topBar = { ItemListTopBar() },
        floatingActionButton = { AddItemFAB(onClick = onAddClick) }
    ) { padding ->
        when (uiState) {
            is Loading -> LoadingIndicator()
            is Success -> ItemList(items = uiState.items, onItemClick)
            is Error -> ErrorMessage(message = uiState.message)
        }
    }
}
```

#### **Reusable UI Components**

```kotlin
@Composable
fun ItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // Item details
    }
}
```

### UI State Management

#### **UI State Classes**

```kotlin
sealed interface ItemListUiState {
    object Loading : ItemListUiState
    data class Success(val items: List<Item>) : ItemListUiState
    data class Error(val message: String) : ItemListUiState
}
```

#### **State in ViewModels**

```kotlin
class ItemListViewModel @Inject constructor(
    private val getAllItemsUseCase: GetAllItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ItemListUiState>(Loading)
    val uiState: StateFlow<ItemListUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            getAllItemsUseCase()
                .catch { exception ->
                    _uiState.value = Error(exception.message ?: "Unknown error")
                }
                .collect { items ->
                    _uiState.value = Success(items)
                }
        }
    }
}
```

### Navigation

**Compose Navigation**: Type-safe navigation between screens

```kotlin
@Composable
fun InventoryNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController, startDestination = "item_list") {
        composable("item_list") {
            ItemListScreen(
                onItemClick = { item ->
                    navController.navigate("item_detail/${item.id}")
                }
            )
        }
        composable("item_detail/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            ItemDetailScreen(itemId = itemId?.toLongOrNull())
        }
    }
}
```

### Theming & Styling

**Material 3 Theme**:
- Dynamic color from wallpaper (Android 12+)
- Custom color scheme for older devices
- Dark/Light mode support
- Typography system (headings, body, labels)

---

## 💉 Dependency Injection with Hilt

### Module Structure

#### **Application Module**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideInventoryDatabase(
        @ApplicationContext context: Context
    ): InventoryDatabase {
        return Room.databaseBuilder(
            context,
            InventoryDatabase::class.java,
            "inventory_db"
        ).build()
    }

    @Provides
    fun provideItemDao(database: InventoryDatabase): ItemDao {
        return database.itemDao()
    }
}
```

#### **Repository Module**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindItemRepository(
        impl: ItemRepositoryImpl
    ): ItemRepository
}
```

#### **Use Case Module**

```kotlin
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetAllItemsUseCase(
        repository: ItemRepository
    ): GetAllItemsUseCase {
        return GetAllItemsUseCase(repository)
    }
}
```

### Scopes

- **@Singleton**: Application-level (Database, Repositories)
- **@ViewModelScoped**: ViewModel lifecycle (Use Cases)
- **@ActivityRetainedScoped**: Activity retained lifecycle
- **@ActivityScoped**: Activity lifecycle

### Testing Support

Hilt provides `@HiltAndroidTest` for integration tests:

```kotlin
@HiltAndroidTest
class ItemRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: ItemRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testInsertItem() = runTest {
        // Test implementation
    }
}
```

---

## 📦 Module Structure

### Package Organization

```
com.inventory.app/
│
├── data/                          # Data Layer
│   ├── local/
│   │   ├── database/
│   │   │   ├── InventoryDatabase.kt
│   │   │   └── entities/
│   │   │       └── ItemEntity.kt
│   │   └── dao/
│   │       └── ItemDao.kt
│   ├── repository/
│   │   └── ItemRepositoryImpl.kt
│   └── mapper/
│       └── ItemMapper.kt
│
├── domain/                        # Domain Layer
│   ├── model/
│   │   └── Item.kt
│   ├── repository/
│   │   └── ItemRepository.kt (interface)
│   └── usecase/
│       ├── GetAllItemsUseCase.kt
│       ├── AddItemUseCase.kt
│       └── UpdateItemUseCase.kt
│
├── presentation/                  # Presentation Layer
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── itemlist/
│   │   │   │   ├── ItemListScreen.kt
│   │   │   │   ├── ItemListViewModel.kt
│   │   │   │   └── ItemListUiState.kt
│   │   │   └── itemdetail/
│   │   │       ├── ItemDetailScreen.kt
│   │   │       └── ItemDetailViewModel.kt
│   │   ├── components/
│   │   │   ├── ItemCard.kt
│   │   │   └── SearchBar.kt
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Theme.kt
│   │   │   └── Type.kt
│   │   └── navigation/
│   │       └── NavGraph.kt
│
├── di/                            # Dependency Injection
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
│
└── InventoryApplication.kt        # Application Class
```

### Layer Responsibilities

#### **Data Layer** (`data/`)
- Database access
- Repository implementations
- External API clients (future)
- Data source management
- Entity-to-model mapping

#### **Domain Layer** (`domain/`)
- Business logic (Use Cases)
- Domain models (pure Kotlin classes)
- Repository interfaces
- Business rules and validation
- No Android dependencies

#### **Presentation Layer** (`presentation/`)
- UI components (Composables)
- ViewModels
- UI state definitions
- Navigation logic
- Theme and styling

#### **DI Layer** (`di/`)
- Hilt modules
- Dependency provisioning
- Scope definitions

### Dependency Rules

```
Presentation → Domain → Data
     ↓           ↓        ↓
   Hilt       Hilt     Hilt
```

**Strict Rules**:
- Presentation can depend on Domain
- Domain can depend on Data (interfaces only)
- Data implements Domain interfaces
- Domain has NO dependencies on Android framework
- Data can depend on Android framework (Room, etc.)

---

## 🎯 Design Principles

### 1. **Clean Architecture**
- Clear separation of concerns across layers
- Domain layer is framework-agnostic
- Easy to test each layer independently
- Changes in one layer don't ripple to others

### 2. **Offline-First Design**
- Local database is the primary data source
- All core features work without connectivity
- Sync to cloud is additive, not required
- User can always access their data

### 3. **Single Responsibility Principle**
- Each class has one reason to change
- ViewModels handle UI logic only
- Use Cases handle single business operations
- Repositories handle data access only

### 4. **Dependency Inversion**
- High-level modules don't depend on low-level modules
- Both depend on abstractions (interfaces)
- Hilt provides concrete implementations at runtime

### 5. **Don't Repeat Yourself (DRY)**
- Reusable composables for common UI patterns
- Shared base Use Cases for common operations
- Mapper functions for data transformations

### 6. **SOLID Principles**
- **S**ingle Responsibility
- **O**pen/Closed (open for extension, closed for modification)
- **L**iskov Substitution (subtypes are substitutable)
- **I**nterface Segregation (small, focused interfaces)
- **D**ependency Inversion (depend on abstractions)

### 7. **Reactive Programming**
- UI reacts to state changes automatically
- Flow/StateFlow for reactive data streams
- ViewModel exposes state, View observes and renders

### 8. **Testability First**
- Write testable code from the start
- Use dependency injection for easy mocking
- Pure functions where possible
- Separate business logic from framework code

---

## 🔮 Future Considerations

### Scalability Enhancements

#### **Multi-Module Architecture**
When the app grows, consider splitting into Gradle modules:
```
:app                    (Application module)
:feature:itemlist       (Item list feature)
:feature:itemdetail     (Item detail feature)
:core:data              (Data layer)
:core:domain            (Domain layer)
:core:ui                (Shared UI components)
```

**Benefits**:
- Faster build times (parallel compilation)
- Better separation of features
- Easier team collaboration
- Forced architectural boundaries

#### **Cloud Synchronization**
- **Architecture**: Local-first with background sync
- **Sync Strategy**: Eventual consistency with conflict resolution
- **Implementation**: WorkManager for background sync jobs
- **Conflict Resolution**: Last-write-wins or manual merge

#### **Multi-User Support**
- User authentication (Firebase Auth, custom backend)
- Role-based access control (RBAC)
- Shared inventory across team members
- Activity logging and audit trails

### Performance Optimizations

#### **Database Optimization**
- Pagination for large datasets (Paging 3 library)
- Indexed columns for frequently queried fields
- Database triggers for complex operations
- Query optimization and profiling

#### **UI Performance**
- LazyColumn for efficient list rendering
- Image caching for item photos
- Background processing for heavy operations
- Debouncing for search queries

### Advanced Features

#### **Barcode Scanning Enhancements**
- Batch scanning mode
- Custom barcode generation
- QR code support for item sharing

#### **Analytics & Reporting**
- Inventory value calculations
- Stock movement trends
- Low-stock predictions
- Custom report generation

#### **Export/Import**
- Multiple format support (CSV, Excel, JSON)
- Bulk import with validation
- Scheduled exports
- Cloud backup integration

### Technology Evolution

#### **Kotlin Multiplatform (KMP)**
- Share business logic between Android and iOS
- Keep UI platform-specific
- Reduce duplication for cross-platform apps

#### **Jetpack Compose Multiplatform**
- Share UI code across Android, iOS, Desktop, Web
- Evaluate when maturity reaches production-ready state

#### **AI/ML Integration**
- Smart categorization suggestions
- Demand forecasting
- Anomaly detection (unusual stock changes)
- Image recognition for item identification

### Security Considerations

#### **Data Encryption**
- SQLCipher for encrypted database
- Secure storage for sensitive data (keystore)
- Certificate pinning for API calls

#### **Authentication & Authorization**
- Biometric authentication
- OAuth 2.0 for cloud services
- Token-based session management

---

## 📚 References & Resources

### Official Documentation
- [Android Developers Guide](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

### Architecture Patterns
- [Guide to App Architecture](https://developer.android.com/topic/architecture)
- [MVVM Pattern](https://developer.android.com/topic/architecture#recommended-app-arch)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

### Best Practices
- [Android Best Practices](https://developer.android.com/topic/architecture/recommendations)
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Material Design Guidelines](https://m3.material.io/)

---

**Document Version**: 1.0
**Last Updated**: March 2026
**Maintained By**: Inventory APK Development Team
