/**
 * Copyright (C) 2020 by Qifan YANG (@underwindfall)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package underwindfall.qifan

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import java.lang.reflect.Type
import java.time.Instant
import kotlin.reflect.KClass

interface GitHubApi {
  @GET("/users/{login}/events")
  suspend fun getUserActivity(@Path("login") login: String): List<GitHubActivityEvent>

  companion object {
    fun create(
      client: OkHttpClient,
      moshi: Moshi
    ): GitHubApi {
      return Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .validateEagerly(true)
        .client(client)
        .addConverterFactory(
          MoshiConverterFactory.create(
            moshi.newBuilder()
              .add(
                DefaultOnDataMismatchAdapter.newFactory(
                  GitHubActivityEventPayload.Type::class.java,
                  GitHubActivityEventPayload.Type.UNKNOWN
                )
              )
              .add(GitHubActivityEvent.Factory)
              .build()
          )
        )
        .build()
        .create()
    }
  }
}

data class GitHubActivityEvent(
  val id: String,
  val createdAt: Instant,
  val payload: GitHubActivityEventPayload?,
  val public: Boolean,
  val repo: Repo?
) {
  companion object Factory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
      if (Types.getRawType(type) != GitHubActivityEvent::class.java) return null
      if (annotations.isNotEmpty()) return null

      val typeAdapter = moshi.adapter(GitHubActivityEventPayload.Type::class.java)
      val repoAdapter = moshi.adapter(Repo::class.java)
      return object : JsonAdapter<GitHubActivityEvent>() {
        override fun fromJson(reader: JsonReader): GitHubActivityEvent {
          @Suppress("UNCHECKED_CAST")
          val value = reader.readJsonValue() as Map<String, *>
          val payloadType = value["type"]?.toString()?.let(typeAdapter::fromJsonValue) ?: error("No type found")
          val payloadValue = value["payload"]
          val payload = if (payloadType != GitHubActivityEventPayload.Type.UNKNOWN && payloadValue != null) {
            moshi.adapter(payloadType.subclass.java)
              .fromJsonValue(payloadValue)
          } else {
            null
          }
          val id = value["id"]?.toString() ?: error("No id found")
          val createdAt = value["created_at"]?.toString() ?: error("No created_at found")
          val public = value["public"]?.toString()?.toBoolean() ?: error("No public found")
          val repo = value["repo"]?.let { repoAdapter.fromJsonValue(it) }

          val createdAtInstant = Instant.parse(createdAt)
          return GitHubActivityEvent(id, createdAtInstant, payload, public, repo)
        }

        override fun toJson(writer: JsonWriter, value: GitHubActivityEvent?) {
          throw NotImplementedError()
        }
      }
    }
  }
}

sealed interface GitHubActivityEventPayload {
  enum class Type(val subclass: KClass<out GitHubActivityEventPayload>) {
    UNKNOWN(UnknownPayload::class),

    @Json(name = "IssuesEvent")
    ISSUE(IssuesEventPayload::class),

    @Json(name = "IssueCommentEvent")
    ISSUE_COMMENT(IssueCommentEventPayload::class),

    @Json(name = "PushEvent")
    PUSH(PushEventPayload::class),

    @Json(name = "PullRequestEvent")
    PULL_REQUEST(PullRequestPayload::class),

    @Json(name = "CreateEvent")
    CREATE_EVENT(CreateEvent::class),

    @Json(name = "DeleteEvent")
    DELETE_EVENT(DeleteEvent::class)
  }
}

object UnknownPayload : GitHubActivityEventPayload

@JsonClass(generateAdapter = true)
data class IssuesEventPayload(
  val action: String,
  val issue: Issue
) : GitHubActivityEventPayload

@JsonClass(generateAdapter = true)
data class Issue(
  val title: String,
  val body: String?,
  val url: String,
  val number: Int
)

@JsonClass(generateAdapter = true)
data class IssueCommentEventPayload(
  val action: String,
  val comment: Comment,
  val issue: Issue
) : GitHubActivityEventPayload

@JsonClass(generateAdapter = true)
data class Comment(
  @Json(name = "html_url")
  val htmlUrl: String,
  val body: String?
)

@JsonClass(generateAdapter = true)
data class PushEventPayload(
  val head: String,
  val size: Int,
  @Json(name = "distinct_size")
  val distinctSize: Int,
  val commits: List<Commit>
) : GitHubActivityEventPayload {
  fun commitMessage(event: GitHubActivityEvent): String {
    return if (distinctSize == 1) {
      val commit = commits[0]
      // "pushed [`${commit.sha.substring(0..7)}`](${commit.adjustedUrl()}) to ${event.repo?.markdownUrl()}: \"${commit.title()}\""
      "pushed to ${event.repo?.markdownUrl()}: \"${commit.title()}\""
    } else {
      "pushed $size commits to ${event.repo?.markdownUrl()}."
    }
  }
}

@JsonClass(generateAdapter = true)
data class Commit(
  val sha: String,
  val message: String,
  val url: String
) {
  fun title(): String = message.substringBefore("\n")
  fun adjustedUrl(): String = url.replace("api.", "").replace("/repos/", "/").replace("/commits/", "/commit/")
}

@JsonClass(generateAdapter = true)
data class PullRequestPayload(
  val action: String,
  val number: Int,
  @Json(name = "pull_request")
  val pullRequest: PullRequest
) : GitHubActivityEventPayload

@JsonClass(generateAdapter = true)
data class PullRequest(
  val url: String,
  val title: String,
  val body: String?
)

@JsonClass(generateAdapter = true)
data class Repo(
  val name: String,
  val url: String
) {
  fun markdownUrl(): String = "[$name]($url)"
}

@JsonClass(generateAdapter = true)
data class CreateEvent(
  val ref: String?,
  @Json(name = "ref_type")
  val refType: String
) : GitHubActivityEventPayload

@JsonClass(generateAdapter = true)
data class DeleteEvent(
  val ref: String?,
  @Json(name = "ref_type")
  val refType: String
) : GitHubActivityEventPayload
