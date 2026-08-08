package id.my.matahati.audit.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // Dashboard
    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummaryResponse>

    // Audit Categories
    @GET("api/audit/categories")
    suspend fun getCategories(): Response<CategoryListResponse>

    @GET("api/audit/categories/{id}")
    suspend fun getCategory(
        @Path("id") id: Int
    ): Response<CategoryResponse>

    @POST("api/audit/categories")
    suspend fun createCategory(
        @Body request: CategoryRequest
    ): Response<CategoryResponse>

    @POST("api/audit/categories/{id}/update")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: CategoryRequest
    ): Response<CategoryResponse>

    @POST("api/audit/categories/{id}/delete")
    suspend fun deleteCategory(
        @Path("id") id: Int
    ): Response<CategoryResponse>

    // Audit Questions
    @GET("api/audit/categories/{categoryId}/questions")
    suspend fun getQuestions(
        @Path("categoryId") categoryId: Int
    ): Response<QuestionListResponse>

    @POST("api/audit/questions")
    suspend fun createQuestion(
        @Body request: QuestionRequest
    ): Response<QuestionResponse>

    @POST("api/audit/questions/{id}")
    suspend fun updateQuestion(
        @Path("id") id: Int,
        @Body request: QuestionRequest
    ): Response<QuestionResponse>

    @POST("api/audit/questions/{id}/delete")
    suspend fun deleteQuestion(
        @Path("id") id: Int
    ): Response<QuestionResponse>

    @POST("api/audit/questions/reorder")
    suspend fun reorderQuestions(
        @Body request: ReorderRequest
    ): Response<QuestionResponse>

    // Audit Departments & Mapping
    @GET("api/audit/departments")
    suspend fun getDepartments(): Response<DepartmentListResponse>

    @GET("api/audit/departments/{id}/mapping")
    suspend fun getDepartmentMapping(
        @Path("id") id: Int
    ): Response<DepartmentMappingResponse>

    @POST("api/audit/departments/mapping")
    suspend fun saveDepartmentMapping(
        @Body request: SaveMappingRequest
    ): Response<GenericResponse>

    // Audit Execution
    @POST("api/audits/create")
    suspend fun createAudit(
        @Body request: AuditCreateRequest
    ): Response<AuditCreateResponse>

    @GET("api/audits/detail")
    suspend fun getAuditDetail(
        @retrofit2.http.Query("id") auditId: Int
    ): Response<AuditDetailResponse>

    @POST("api/audits/update")
    suspend fun updateAudit(
        @Body request: AuditUpdateRequest
    ): Response<AuditUpdateResponse>

    @retrofit2.http.Multipart
    @POST("api/audits/upload-photo")
    suspend fun uploadAuditPhoto(
        @retrofit2.http.Part("audit_id") auditId: okhttp3.RequestBody,
        @retrofit2.http.Part("response_id") responseId: okhttp3.RequestBody,
        @retrofit2.http.Part photo: okhttp3.MultipartBody.Part
    ): Response<AuditUpdateResponse>

    @POST("api/audits/update-photo")
    suspend fun updateAuditPhoto(
        @Body request: AuditPhotoUpdateData
    ): Response<AuditUpdateResponse>

    @POST("api/audits/delete-photo")
    suspend fun deleteAuditPhoto(
        @Body request: AuditDeletePhotoRequest
    ): Response<AuditUpdateResponse>

    @retrofit2.http.Multipart
    @POST("api/audits/submit")
    suspend fun submitAudit(
        @retrofit2.http.Part("audit_id") auditId: okhttp3.RequestBody,
        @retrofit2.http.Part("auditee_name") auditeeName: okhttp3.RequestBody,
        @retrofit2.http.Part verificationPhoto: okhttp3.MultipartBody.Part
    ): Response<AuditSubmitResponse>

    @GET("api/audits")
    suspend fun getAudits(
        @retrofit2.http.Query("department_id") departmentId: Int?,
        @retrofit2.http.Query("date_from") dateFrom: String?,
        @retrofit2.http.Query("date_to") dateTo: String?
    ): Response<AuditHistoryResponse>

    @POST("api/audits/delete")
    suspend fun deleteAudit(
        @Body request: GenericIdRequest
    ): Response<AuditUpdateResponse>

    // Stock Management
    @GET("api/stock/categories")
    suspend fun getStockCategories(): Response<StockCategoryListResponse>

    @POST("api/stock/categories")
    suspend fun createStockCategory(
        @Body request: StockCategoryRequest
    ): Response<StockCategoryResponse>

    @POST("api/stock/categories/{id}")
    suspend fun updateStockCategory(
        @Path("id") id: Int,
        @Body request: StockCategoryRequest
    ): Response<StockCategoryResponse>

    @POST("api/stock/categories/{id}")
    suspend fun deleteStockCategory(
        @Path("id") id: Int,
        @Body request: StockDeleteRequest
    ): Response<GenericResponse>

    @POST("api/stock/items")
    suspend fun createStockItem(
        @Body request: StockItemRequest
    ): Response<StockItemResponse>

    @POST("api/stock/items/{id}")
    suspend fun deleteStockItem(
        @Path("id") id: Int,
        @Body request: StockDeleteRequest
    ): Response<GenericResponse>

    @POST("api/stock/items/reorder")
    suspend fun reorderStockItems(
        @Body request: StockReorderRequest
    ): Response<GenericResponse>

    // Stock Detail Items
    @GET("api/stock/categories/{categoryId}/items")
    suspend fun getStockItems(
        @Path("categoryId") categoryId: Int
    ): Response<StockCategoryResponse>

    // Stock Department Mapping
    @GET("api/stock/departments")
    suspend fun getStockDepartments(): Response<DepartmentListResponse>

    @GET("api/stock/departments/{id}/mapping")
    suspend fun getStockDepartmentMapping(
        @Path("id") id: Int
    ): Response<StockDepartmentMappingResponse>

    @POST("api/stock/departments/mapping")
    suspend fun saveStockDepartmentMapping(
        @Body request: SaveStockMappingRequest
    ): Response<GenericResponse>

}
