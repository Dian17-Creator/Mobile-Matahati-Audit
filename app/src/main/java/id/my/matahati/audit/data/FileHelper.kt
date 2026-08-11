package id.my.matahati.audit.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileHelper {

    fun saveAndOpenPdf(context: Context, body: ResponseBody, fileName: String): ApiResult<Unit> {
        return try {
            val file = File(context.cacheDir, "$fileName.pdf")
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                val fileReader = ByteArray(4096)
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) break
                    outputStream.write(fileReader, 0, read)
                }
                outputStream.flush()
            } finally {
                inputStream?.close()
                outputStream?.close()
            }

            openPdf(context, file)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Gagal menyimpan atau membuka file: ${e.localizedMessage}")
        }
    }

    private fun openPdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
