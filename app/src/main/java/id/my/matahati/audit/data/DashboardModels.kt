package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class RecentActivityData(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("status") val status: String
)

data class DashboardSummaryData(
    @SerializedName("total_kategori") val totalKategori: Int,
    @SerializedName("total_pertanyaan") val totalPertanyaan: Int,
    @SerializedName("total_audit") val totalAudit: Int,
    @SerializedName("total_kategori_stok") val totalKategoriStok: Int,
    @SerializedName("total_barang") val totalBarang: Int,
    @SerializedName("total_stok_opname") val totalStokOpname: Int,
    @SerializedName("recent_activity") val recentActivity: List<RecentActivityData>?
)

data class DashboardSummaryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: DashboardSummaryData?
)
