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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.time.Instant
import java.time.ZoneId
import kotlin.system.exitProcess

class UpdateReadmeCommand : CliktCommand() {
    val outputFile by option("-o", help = "The README.md file to write")
            .file()
            .required()

    override fun run() {
        val okHttpClient = OkHttpClient.Builder()
                .build()
        val githubActivity = fetchGithubActivity(okHttpClient)
        val list = listOf(
                FeedItem("Markdown           31 mins  ████████████████████▒░░░  71.1%\n"),
                FeedItem("Bash               10 mins  █████████▒░░░░░░░░░░░░░░  24.6%\n"),
                FeedItem("JavaScript          0 secs  ███▓░░░░░░░░░░░░░░░░░░░░   1.4%\n"),
                FeedItem("HTML                0 secs  ███▓░░░░░░░░░░░░░░░░░░░░   1.0%\n"),
                FeedItem("Other               0 secs  ███▓░░░░░░░░░░░░░░░░░░░░   0.8%\n"),
                FeedItem("Java                0 secs  ███▓░░░░░░░░░░░░░░░░░░░░   0.7%"))


        val newReadMe = createReadMe(githubActivity, list)
        outputFile.writeText(newReadMe)

        exitProcess(0)
    }
}

private fun fetchCodeTimeActivity(client: OkHttpClient): List<FeedItem> {
    val codeTimeApi = CodeTimeApi.create(client)
    val activity = runBlocking { codeTimeApi.getCodeTime("377ee88ba1fabd1e93516e48ca9c61eb") }
    return listOf(FeedItem(activity.body()?.string()?.replace("%", "%/n") ?: ""))
}

private fun fetchGithubActivity(client: OkHttpClient): List<FeedItem> {
    val moshi = Moshi.Builder().build()
    val githubApi = GitHubApi.create(client, moshi)
    val activity = runBlocking { githubApi.getUserActivity("underwindfall") }
    return activity
            .filter { it.public }
            .mapNotNull { event ->
                when (val payload = event.payload) {
                    UnknownPayload, null -> return@mapNotNull null
                    is IssuesEventPayload -> {
                        FeedItem(
                                "${payload.action} issue [#${payload.issue.number}](${payload.issue.url}) on ${event.repo?.markdownUrl()}: \"${payload.issue.title}\"",
                                event.createdAt
                        )
                    }
                    is IssueCommentEventPayload -> {
                        FeedItem(
                                "commented on [#${payload.issue.number}](${payload.comment.htmlUrl}) in ${event.repo?.markdownUrl()}",
                                event.createdAt
                        )
                    }
                    is PushEventPayload -> {
                        FeedItem(
                                payload.commitMessage(event),
                                event.createdAt
                        )
                    }
                    is PullRequestPayload -> {
                        FeedItem(
                                "${payload.action} PR [#${payload.number}](${payload.pullRequest.url}) to ${event.repo?.markdownUrl()}: \"${payload.pullRequest.title}\"",
                                event.createdAt
                        )
                    }
                    is CreateEvent -> {
                        FeedItem(
                                "created ${payload.refType}${payload.ref?.let { " \"$it\"" } ?: ""} on ${event.repo?.markdownUrl()}",
                                event.createdAt
                        )
                    }
                    is DeleteEvent -> {
                        FeedItem(
                                "deleted ${payload.refType}${payload.ref?.let { " \"$it\"" } ?: ""} on ${event.repo?.markdownUrl()}",
                                event.createdAt
                        )
                    }
                }
            }
            .take(10)
}

fun main(argv: Array<String>) {
    UpdateReadmeCommand().main(arrayOf("-o README.md"))
}


data class FeedItem(
        val content: String,
        val timestamp: Instant? = null
) {
    override fun toString(): String {
        return if (timestamp == null) {
            content
        } else {
            "**${timestamp.atZone(ZoneId.of("Asia/Shanghai")).toLocalDate()}** — $content"
        }
    }
}