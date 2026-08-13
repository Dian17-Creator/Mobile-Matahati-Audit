# Implementation Plan: Comprehensive Local Cache Extension

Extend the local caching mechanism to all major entities in the application to optimize performance and provide better offline resilience using the "Cache-first + Background Refresh" pattern.

## User Review Required

> [!IMPORTANT]
> All primary read methods in Repositories will be converted to return `Flow<ApiResult<T>>`. ViewModels will be converted to `AndroidViewModel` to provide the necessary `Context` for cache management. This is a significant change affecting multiple files but follows the established pattern from the Department Mapping update.

## Proposed Changes

### [MODIFY] [DataCacheManager.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/DataCacheManager.kt)

- Add more specialized keys if necessary, or keep the generic approach.
- The existing implementation is already generic enough.

---

### [Component] Dashboard & Stats

#### [MODIFY] [DashboardRepository.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/repository/DashboardRepository.kt)
- `getDashboardSummary()` -> Returns `Flow`.
- Cache key: `dashboard_summary`.

#### [MODIFY] [HomeViewModel.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/viewmodel/HomeViewModel.kt)
- Convert to `AndroidViewModel`.
- Update `fetchDashboardSummary()` to collect Flow.

#### [MODIFY] [StockViewModel.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/viewmodel/StockViewModel.kt)
- Convert to `AndroidViewModel`.
- Update `fetchDashboardSummary()` to collect Flow.

---

### [Component] Audit Categories & Questions

#### [MODIFY] [AuditCategoryRepository.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/repository/AuditCategoryRepository.kt)
- `getCategories()` -> Returns `Flow`. Cache key: `audit_categories`.
- Invalidate cache on `createCategory`, `updateCategory`, and `deleteCategory`.

#### [MODIFY] [AuditCategoryViewModel.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/viewmodel/AuditCategoryViewModel.kt)
- Convert to `AndroidViewModel`.
- Update `fetchCategories()` to collect Flow.

#### [MODIFY] [AuditQuestionRepository.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/repository/AuditQuestionRepository.kt)
- `getQuestions(categoryId)` -> Returns `Flow`. Cache key: `audit_questions_{categoryId}`.
- Invalidate relevant category cache on `createQuestion`, `updateQuestion`, `deleteQuestion`, and `reorderQuestions`.

#### [MODIFY] [AuditQuestionViewModel.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/viewmodel/AuditQuestionViewModel.kt)
- Convert to `AndroidViewModel`.
- Update `fetchQuestions()` to collect Flow.

---

### [Component] Stock Categories & Items

#### [MODIFY] [StockRepository.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/repository/StockRepository.kt)
- `getCategories()` -> Returns `Flow`. Cache key: `stock_categories`.
- `getCategory(id)` (which fetches items) -> Returns `Flow`. Cache key: `stock_items_{id}`.
- Invalidate relevant caches on write operations.

#### [MODIFY] [StockCategoryViewModel.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/viewmodel/StockCategoryViewModel.kt)
- Convert to `AndroidViewModel`.
- Update `fetchCategories()` to collect Flow.

#### [MODIFY] [StockItemViewModel.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/viewmodel/StockItemViewModel.kt)
- Convert to `AndroidViewModel`.
- Update `fetchItems()` to collect Flow.

---

### [Component] Audit & Stock Opname History/Detail

#### [MODIFY] [AuditExecutionRepository.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/repository/AuditExecutionRepository.kt)
- `getAudits(...)` -> Returns `Flow`. Cache key: `audit_history_{deptId}_{dateFrom}_{dateTo}`.
- `getAuditDetail(id)` -> Returns `Flow`. Cache key: `audit_detail_{id}`.

#### [MODIFY] [StockOpnameRepository.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/repository/StockOpnameRepository.kt)
- `getStockOpnameHistories(...)` -> Returns `Flow`. Cache key: `stock_opname_history_{auditorId}_{deptId}_{dateFrom}_{dateTo}`.
- `getStockOpnameDetail(id, auditorId)` -> Returns `Flow`. Cache key: `stock_opname_detail_{id}`.

---

## Verification Plan

### Automated Tests
- Verification will be done by code analysis to ensure all write operations invalidate the correct cache keys and all read operations follow the cache-first pattern.

### Manual Verification
1.  **Dashboard**: Open home/stock screen. Totals and recent activities should appear instantly.
2.  **Categories/Items**: Manage categories/items. Changes should invalidate cache and refresh correctly.
3.  **Audit/Stock Opname**: View history and details. Previously viewed documents should open instantly from cache.
4.  **Offline Mode**: Test navigation to various screens while airplane mode is on. Data previously fetched should still be visible.
