# ILC Android 專案架構重構實作計畫 (YAGNI & Inline 版)

本計劃旨在參考 `EquipInspectAndroid` 的 MVVM 分層規範重構 `ILC` Android 專案。

為了嚴格遵守 **YAGNI 原則 (You Aren't Gonna Need It)** 與 **Inline 原則**，我們**不引入 Dagger Hilt、KSP、BaseFragment 或多餘的 Repository 介面**，因為目前的專案不需要這些複雜設計即可正常運作。

本次重構的核心目標是**將 View 與 Model 徹底解耦（落實 MVVM 分層），並整理 ViewModel 的檔案結構，同時保持 UI 佈局 (XML) 和所有功能流程完全原樣**。

---

## User Review Required

> [!IMPORTANT]
> * **無 Gradle 依賴變更**：本次重構不修改 `build.gradle.kts` 或 `libs.versions.toml`，不增加額外的第三方套件，以保持專案的簡單度。
> * **解耦 View 與 MockData**：將原本 Fragment 直接存取 `MockData` 的寫法，改由 `AppViewModel` 統一代理與暴露，使 UI 與數據來源完全解耦。

---

## Proposed Changes

### UI & ViewModel Layer

#### [NEW] [NavViewModel.kt](file:///c:/Users/Administrator/Documents/GitHub/ycs-ilc-android/ILC/app/src/main/java/com/yuchens/ilcandroid/ui/viewmodel/NavViewModel.kt)
* 將 `NavViewModel` 從 `AppViewModel.kt` 中拆分出來，獨立成此檔案，使職責分明。

#### [MODIFY] [AppViewModel.kt](file:///c:/Users/Administrator/Documents/GitHub/ycs-ilc-android/ILC/app/src/main/java/com/yuchens/ilcandroid/ui/viewmodel/AppViewModel.kt)
* 移除 `NavViewModel` 宣告。
* 新增代理 MockData 的屬性與方法，例如將 `shifts`、`drivers`、`resolveStaffLabel()` 等暴露出來，供 Fragment 使用。

#### [MODIFY] [DashboardFragment.kt](file:///c:/Users/Administrator/Documents/GitHub/ycs-ilc-android/ILC/app/src/main/java/com/yuchens/ilcandroid/ui/fragment/DashboardFragment.kt)
* 保持與 `AppViewModel` 互動，移除任何潛在對 `MockData` 的直接引用（如果有）。

#### [MODIFY] [PunchFragment.kt](file:///c:/Users/Administrator/Documents/GitHub/ycs-ilc-android/ILC/app/src/main/java/com/yuchens/ilcandroid/ui/fragment/PunchFragment.kt)
* 將原先直接存取 `MockData.shifts`、`MockData.staffList` 的程式碼，修改為由 `appVm` 取得，以符合 MVVM 規範。

#### [MODIFY] [WorkflowFragment.kt](file:///c:/Users/Administrator/Documents/GitHub/ycs-ilc-android/ILC/app/src/main/java/com/yuchens/ilcandroid/ui/fragment/WorkflowFragment.kt)
* 將原先在步驟值顯示 (e.g., `MockData.resolveStaffLabel`) 和步驟輸入選項 (e.g., `MockData.drivers`) 的直接存取，全數改為經由 `appVm` 的對應代理方法/屬性取得。

---

## Verification Plan

### Automated Tests
* 執行 `./gradlew assembleDebug` 確保專案能正常編譯且無語法錯誤。

### Manual Verification
* 測試專案功能流程，確認與重構前行為完全一致：
  1. 開啟 App，選擇角色登入。
  2. 登入確認成功，依角色切換底部分頁導覽列。
  3. 執行出工流程、進場流程、離場流程、以及值班打卡。
  4. 確認資料狀態（如停車格佔用與釋放）顯示正確。
