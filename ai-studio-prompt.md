# Google AI Studio — Android App 開發 Prompt

> 用途：將以下整段內容貼到 Google AI Studio（或任何 LLM coding agent），請它從零產出一個原型 Android App。
> 因為目標平台無法存取原始參照專案，本 Prompt 已把架構、SDK、套件版本、設計規範與功能規格全部具體寫死。

---

請依照以下完整規格，從零開發一個 **Android 原生 App（Kotlin）**。這是一個給「現場手持式 PDA / 觸控裝置」使用的作業管理原型，所有資料一律使用 **Mock Data（記憶體假資料）**，不需串接真實後端。

## 0. 專案定位

- App 名稱：**台積電 ILC 化學品槽車轉運站 — PDA 行動管理系統**
- 性質：**互動原型（Prototype）**。所有輸入採「模擬輸入」：點輸入欄位後，提供虛擬數字鍵盤或常用預設快選選單，確認後填入 Mock Data 並前進到下一步驟。
- 介面語言：**繁體中文為主、英文術語為輔的雙語對照**（例如：`移轉作業 / Transfer`、`廠區 / Fab`）。所有文字一律集中放在 `strings.xml`，禁止 hardcode 字串於程式或版面。
- 操作情境：使用者單手持 PDA 操作，因此所有可點擊元件最小高度 **48dp**，主要字級 **20sp**，按鈕字級 **18–20sp 並加粗**。

---

## 1. 技術棧與 SDK 設定（請完全照抄版本）

### 1.1 建置工具
- Android Gradle Plugin (AGP)：**8.6.1**
- Gradle Wrapper：**8.13**
- Kotlin：**2.1.10**（KSP：`2.1.10-1.0.29`）
- JDK / JVM target：**17**（`sourceCompatibility`、`targetCompatibility`、`jvmTarget` 全部 17）
- 使用 **Kotlin DSL（`build.gradle.kts`）** 與 **Version Catalog（`gradle/libs.versions.toml`）** 管理依賴。

### 1.2 SDK
- `compileSdk = 35`
- `minSdk = 26`
- `targetSdk = 35`
- `namespace = "com.yuchens.ilcandroid"`
- `applicationId = "com.yuchens.ilcandroid"`

### 1.3 buildFeatures
- `viewBinding = true`（**強制使用 ViewBinding，禁止 findViewById、禁止 Jetpack Compose**）
- `buildConfig = true`
- `vectorDrawables.useSupportLibrary = true`

### 1.4 版本號邏輯（沿用原專案規則）
```kotlin
val major = 1
val minor = 0
val patch = 0
val buildNumber = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 0
val versionNameComputed = "$major.$minor.$patch"
val versionCodeComputed = (major * 10000 + minor * 100 + patch) * 100 + buildNumber
```
並用 `buildConfigField` 輸出 `APP_VERSION_NAME`、`APP_VERSION_CODE`、`SHOW_BUILD_TYPE`。

### 1.5 依賴套件（版本鎖定）
| 類別 | 套件 | 版本 |
|---|---|---|
| Core KTX | androidx.core:core-ktx | 1.16.0 |
| AppCompat | androidx.appcompat:appcompat | 1.7.1 |
| Material | com.google.android.material:material | 1.12.0 |
| Activity | androidx.activity:activity | 1.10.1 |
| ConstraintLayout | androidx.constraintlayout:constraintlayout | 2.2.1 |
| Fragment KTX | androidx.fragment:fragment-ktx | 1.8.5 |
| Lifecycle (runtime/viewmodel/livedata/process) | androidx.lifecycle:* | 2.8.4 |
| Coroutines (core/android) | org.jetbrains.kotlinx:kotlinx-coroutines-* | 1.9.0 |
| Hilt | com.google.dagger:hilt-android / hilt-android-compiler（KSP） | 2.54 |
| DataStore | androidx.datastore:datastore-preferences | 1.1.7 |
| Serialization | org.jetbrains.kotlinx:kotlinx-serialization-json | 1.7.3 |
| 測試 | junit 4.13.2 / androidx.test.ext:junit 1.3.0 / espresso 3.7.0 | — |

> 原型階段可不引入 Room 與 OkHttp（因為使用 Mock Data）。若要保留資料層分層練習，可選擇性加入 Room 2.6.1（KSP），但預設以記憶體假資料為主。

### 1.6 gradle.properties
```
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
ksp.incremental=true
```

---

## 2. 架構規範（MVVM + Hilt，沿用原專案風格）

### 2.1 分層
- 套件結構：
  ```
  com.yuchens.ilcandroid
  ├─ MyApp.kt                      // @HiltAndroidApp
  ├─ ui/
  │   ├─ LoginActivity.kt          // 角色選擇 + 工號輸入
  │   ├─ MainActivity.kt           // 主框架（單一 Activity + Fragment 切換）
  │   ├─ base/BaseFragment.kt
  │   ├─ fragment/                 // 各分頁與作業流程 Fragment
  │   ├─ viewmodel/                // 每個畫面對應一個 ViewModel
  │   ├─ adapter/                  // RecyclerView Adapter
  │   ├─ model/                    // UI model（如 EquipUi、Option）
  │   └─ widget/                   // 自訂 View（如不彈鍵盤的輸入框）
  ├─ data/
  │   ├─ repository/               // MockRepository（記憶體假資料）
  │   └─ model/                    // 領域資料類別
  ├─ di/                           // Hilt Module
  └─ util/                         // 工具（Prefs、Log 等）
  ```

### 2.2 規範要點
- **依賴注入**：使用 **Hilt**。`MyApp` 標 `@HiltAndroidApp`；Activity / Fragment 標 `@AndroidEntryPoint`；ViewModel 標 `@HiltViewModel` 並用 `@Inject constructor`。Repository 在 `@Module @InstallIn(SingletonComponent::class)` 內以 `@Provides @Singleton` 提供。
- **ViewModel ↔ View 溝通**：狀態用 `StateFlow`、一次性事件（Toast/訊息）用 `SharedFlow`。Fragment 端一律以 `viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { ... collect } }` 收集。
- **ViewBinding 生命週期**：Fragment 使用 `private var _binding: XxxBinding? = null`，`onCreateView` inflate、`onDestroyView` 設為 null。
- **導覽**：採「**單一 Activity + 自訂分頁切換**」。用一個 Activity 作用域的 `NavViewModel`（`by activityViewModels()`）持有目前分頁的 `StateFlow<Tab>`；底部導覽列 Fragment 只負責 `vm.select(tab)`；MainActivity 觀察 Tab 變化後 `replaceFragment()` 換中間內容。**不使用 Navigation Component 的 nav graph**（沿用原專案手動 FragmentTransaction 的做法）。
- **協程**：背景工作切到 `Dispatchers.IO`，更新 UI 切回 `Dispatchers.Main`。
- **設定持久化**：使用 **DataStore Preferences**（例如記住目前角色與工號）。

### 2.3 程式碼風格（重要，務必遵守）
- **YAGNI**：只實作當下需要的功能，不為「未來可能」預先抽象、加介面或 options 參數。
- **Inline First**：三行以內、只用一次的邏輯直接展開，不硬抽函式；用清楚變數名取代多餘抽象層。
- **例外處理克制**：`try/catch` 只包真正的 I/O；不寫空 catch；`?.` 只用在資料來源真的可為 null 處。
- **複雜度**：單一函式圈複雜度 ≤ 5，優先用 guard clause（提早 return）降低巢狀；禁止多層巢狀三元運算子。
- **註解**：只解釋「為什麼」，不寫複述程式碼的廢註解。

---

## 3. 設計系統（沿用原專案視覺語言）

### 3.1 主題
- Theme parent：`Theme.MaterialComponents.DayNight.NoActionBar`（無 ActionBar，採自訂頂部列）。
- StatusBar 顏色綁定 `?attr/colorPrimaryVariant`。
- `supportsRtl = true`。

### 3.2 色彩（`colors.xml`）
```xml
<color name="primary">#333333</color>   <!-- 主色：深灰，用於頂部列、登入背景、主要按鈕 -->
<color name="gray">#ECEFF1</color>      <!-- 內容區背景 -->
<color name="gray_button">#BDBDBD</color> <!-- 次要按鈕（清除） -->
<color name="white">#FFFFFFFF</color>
<color name="black">#FF000000</color>
```
> 整體調性：**深灰 + 白卡片 + 淺灰背景**的乾淨工業風。可為本專案再加一個品牌強調色（如安全橙 `#FB8C00` 或工程藍）用於「進行中任務」「警示」狀態，但主結構維持深灰/白。

### 3.3 版面慣例
- 內容以 **CardView** 承載：`app:cardCornerRadius="12dp"`、`app:cardElevation="6dp"`、`layout_margin="8dp"`，卡片內 `padding="16dp"`。
- 表單列採「左標題（固定寬 `96dp`、`gravity=end`、加粗 `20sp`）+ 右值/輸入（`layout_weight=1`）」的水平 LinearLayout 排列。
- 主要按鈕：`backgroundTint=@color/primary`、`textColor=@color/white`、`textStyle=bold`、`minHeight=48dp`；次要/清除按鈕用 `@color/gray_button`。
- 列表用 **RecyclerView**（搭配自訂 `VerticalSpaceItemDecoration` 做間距）。
- 角色選擇／流程主畫面採「**垂直排列的大按鍵**」，方便手持單手點擊。

### 3.4 自訂 Widget（沿用）
- `NoKeyboardEditText`：繼承 `AppCompatEditText`，覆寫 `onCheckIsTextEditor()` 回傳 false，避免系統鍵盤彈出——本原型用「模擬輸入（虛擬數字鍵盤 / 快選選單）」取代系統鍵盤，正好需要這種輸入框。

---

## 4. 功能規格（核心需求）

### 4.1 角色導向登入
- App 啟動先進 `LoginActivity`，畫面是 **3 個垂直大按鍵**，使用者選擇自身角色：
  1. **ILC 值班人員（ILC Duty Officer）**
  2. **ILC 司機（ILC Driver）**
  3. **廠商司機（Vendor Driver）**
- 選角色後彈出「**工號輸入彈窗**」：含文字欄位 + **虛擬數字鍵盤**，使用者手動輸入個人工號。確認後依角色進入對應的主框架（`MainActivity`），並用 DataStore 記住角色與工號。

### 4.2 底部分頁導覽（依角色切換顯示不同分頁）
- **ILC 值班人員**：首頁 / 移轉作業 / 警報通知 / 排班管理 / 設定
- **ILC 司機**：首頁 / 出工作業 / 進停車場 / 工作紀錄
- **廠商司機**：首頁 / 離場作業 / 歷史紀錄

### 4.3 首頁儀表板（Dashboard，依角色不同）
- **ILC 值班人員**：值班資訊卡（當前值班人員姓名、班別時間、值班狀態）+ 快速鍵區（出工登記、入場登記、離場登記、值班打卡）。
- **ILC 司機**：司機姓名、今日派車（指定任務）、當前任務狀態 + 快速鍵（開始出工、回報入場）。
- **廠商司機**：所屬廠商名稱、分配槽體資訊 + 快速鍵（登記離場）。

### 4.4 出工開始作業（ILC 司機）— 四步驟循序流程
頂部顯示步驟指示器：`① ILC值班 → ② ILC司機 → ③ 槽體 → ④ 廠區`
1. 輸入 ILC 值班人員工號 → 系統回填姓名（例：`H5406340`）。
2. 輸入 ILC 司機工號 → 回填司機姓名（例：`林大宏`）。
3. 輸入槽體編號 → 回填槽體資料（例：`Tank_B`）。
4. 輸入/選擇廠區代碼 → 回填出發廠區（例：`F18A P1`）。
- 最後「確認送出」畫面：摘要卡片列出上述全部輸入，提供「確認送出」「重新輸入」。

### 4.5 進停車場作業（ILC 司機）— 四步驟循序流程
步驟指示器：`① ILC值班 → ② ILC司機 → ③ 槽體 → ④ 停車格`
1~3 同上（值班工號、司機工號、槽體編號）。
4. 輸入停車格編號 → 回填車位（例：`P-06`）。
- 「確認送出」摘要含進場時間與「任務結束」狀態；送出後背景發出通知，提示值班人員聯繫對應廠商派司機取車（原型以 Toast/本地通知模擬）。

### 4.6 廠商離場作業（廠商司機）— 三步驟循序流程
步驟指示器：`① ILC值班 → ② 廠商司機 → ③ 槽體`
1. 輸入 ILC 值班人員工號 → 回填姓名。
2. 輸入廠商司機工號 → 回填司機姓名與所屬廠商（例：`劉家慶 — 長春化工`）。
3. 輸入槽體編號 → 回填槽體資料。
- 「確認送出」摘要含累計停放時間、離場時間；送出後將該停車格狀態釋放為「空位」。

### 4.7 值班打卡（ILC 值班人員）
- **上班打卡**：點「上班打卡」→ 輸入工號 → 確認打卡時間 → 將姓名加入「當前在值班人員清單」。
- **下班打卡**：點「下班打卡」→ 輸入工號 → 確認打卡時間 → 將姓名從清單移除。

### 4.8 全局 SOP（資料流動）
1. 台積電廠區灌充完畢 → ILC 司機登記出工（4.4）。
2. ILC 司機載回轉運站 → 停妥車位並回報進場（4.5）。
3. 廠商司機到場取車 → 辦理離場登記，釋放車位（4.6）。

---

## 5. Mock Data 要求
- 在 `data/repository/MockRepository.kt`（Hilt 提供的 `@Singleton`）內，用記憶體 `MutableStateFlow` / list 維護：
  - 員工清單（工號 → 姓名、角色、所屬廠商）：至少各角色 2~3 筆，含上述範例（`H5406340`、`林大宏`、`劉家慶/長春化工`）。
  - 槽體清單（如 `Tank_A`、`Tank_B`…）。
  - 廠區清單（如 `F18A P1`、`F18B P2`…）。
  - 停車格清單（`P-01`…`P-10`，含空位/占用狀態）。
  - 任務/移轉紀錄清單（供「工作紀錄」「歷史紀錄」「移轉作業」顯示）。
- 「輸入工號 → 回填姓名」「釋放車位」「打卡清單增減」等都直接操作這些記憶體資料並透過 Flow 通知 UI。

---

## 6. 交付要求
1. 可直接以 Android Studio 開啟並 `Build` 成功的完整專案（含 `settings.gradle.kts`、`build.gradle.kts`、`gradle/libs.versions.toml`、`AndroidManifest.xml`、所有資源檔）。
2. `AndroidManifest.xml`：`LoginActivity` 為 LAUNCHER；`MainActivity` `exported=false`；`windowSoftInputMode="stateHidden|adjustPan"`。
3. 三種角色皆可完整走完各自的主要流程（含確認送出與摘要畫面）。
4. 所有字串放 `strings.xml` 並雙語呈現；所有顏色放 `colors.xml`；主題放 `themes.xml`。
5. 程式碼遵守第 2.3 節風格規範。

請先輸出「專案檔案結構樹」，再逐檔輸出完整內容。
```
