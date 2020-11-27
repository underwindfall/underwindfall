package underwindfall.qifan

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path

interface CodeTimeApi {
    @GET("underwindfall/{id}/raw")
    suspend fun getCodeTime(@Path("id") id: String): Response<ResponseBody>

    companion object {
        fun create(
                client: OkHttpClient
        ): CodeTimeApi {
            return Retrofit.Builder()
                    .baseUrl("https://gist.githubusercontent.com/")
                    .validateEagerly(true)
                    .client(client)
                    .build()
                    .create()
        }
    }
}