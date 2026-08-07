# Implementation Plan - Enhance Dashboard Summary with Live Data

Update the Home screen to display live `total_audit` statistics and `recent_activity` from the backend, including a clean empty state for activities.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [DashboardModels.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/data/DashboardModels.kt)
- Add `RecentActivityData` data class with `title`, `subtitle`, and `status`.
- Update `DashboardSummaryData` to include `total_audit` and `recent_activity`.

### [ViewModel Layer]

#### [MODIFY] [HomeViewModel.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/data/viewmodel/HomeViewModel.kt)
- Update `HomeUiState` to include `totalAudit: String` and `recentActivities: List<RecentActivityData>`.
- Update `fetchDashboardSummary()` to map the new fields from the API response.

### [UI Layer]

#### [MODIFY] [AuditHome.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/AuditHome.kt)
- **Stats Section**: Update `SummaryStatsSection` to accept and display the live `totalAudit` value.
- **Activity Section**:
    - Update `RecentActivitySection` to accept the list of activities from `uiState`.
    - Implement conditional rendering: if the list is empty, show the new `EmptyRecentActivity` card; otherwise, loop through the list and display `ActivityItem`.
    - Refactor `ActivityItem` to derive its status color from the `status` string (Green for "Selesai", Blue for "Berjalan").
- **Empty State**: Add `EmptyRecentActivity()` composable with the requested design (Assignment icon, "Belum ada proses audit" title).

## Verification Plan

### Manual Verification
1.  **Stats Sync**: Verify that "AUDIT" card shows the correct number from the API (not hardcoded "3").
2.  **Recent Activity**:
    - If the backend returns activities, verify they appear in the list.
    - If the backend returns an empty list, verify the "Belum ada proses audit" empty state card is displayed.
3.  **Real-time Refresh**: Navigate away from Home, return to Home, and verify that stats and activities refresh immediately.
4.  **Loading & Error**:
    - Verify `--` appears during initial load.
    - Verify `-` appears if the API fails.
