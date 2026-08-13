# Implementation Plan: Local Cache for Department Mapping

Implement a robust local caching mechanism for Department Mapping to improve performance, provide offline support, and reduce redundant API calls.

## User Review Required

> [!IMPORTANT]
> The Repository methods will be changed to return `Flow<ApiResult<T>>` instead of `suspend ApiResult<T>`. This allows emitting multiple states (Cache -> API Update) which is essential for the "instant UI" behavior requested.

## Proposed Changes

### [NEW] [DataCacheManager.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/DataCacheManager.kt)

A utility class using `SharedPreferences` and `Gson` to persist and retrieve generic data models as JSON strings.

- `save(key: String, data: T)`
- `get(key: String, type: Class<T>): T?`
- `delete(key: String)`

### [MODIFY] [AuditDepartmentRepository.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/repository/AuditDepartmentRepository.kt)

Integrate caching logic using `DataCacheManager`.

- Change return types to `Flow<ApiResult<T>>`.
- **Strategy**:
    1. Emit cached data immediately if available.
    2. Fetch from API in the background.
    3. If API succeeds and data differs from cache (or cache is empty), update cache and emit new data.
    4. If API fails but cache exists, keep using cache and don't emit error.
    5. If API fails and no cache exists, emit error.

### [MODIFY] [AuditDepartmentViewModel.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/viewmodel/AuditDepartmentViewModel.kt)

Update ViewModel to handle the new `Flow` responses from the Repository.

- Update `fetchDepartments()` and `fetchMapping()` to collect flows.
- Ensure UI state is updated smoothly without flickering.
- On `saveDepartmentMapping` success, invalidate the cache for that specific department mapping.

### [MODIFY] [RetrofitClientLaravel.kt](file:///D:/Project/Mobile-Matahati-Audit/app/src/main/java/id/my/matahati/audit/data/RetrofitClientLaravel.kt) (If needed)

Ensure Gson is accessible or configured correctly for the repository if needed.

## Verification Plan

### Automated Tests
- Since the environment might not support full Android instrumentation tests easily, I will rely on code analysis and manual verification steps if possible.

### Manual Verification
1.  **Instant UI**: Open Department Mapping. The list should appear instantly if previously loaded.
2.  **Background Refresh**: Observe if any changes in the backend are reflected in the UI after a brief delay without a full-screen loading spinner.
3.  **Offline Support**: Turn off internet and open the page. It should display the last cached data instead of an error screen.
4.  **Cache Invalidation**: Save a mapping, then reopen the page. It should reflect the new changes immediately after the background refresh completes.
