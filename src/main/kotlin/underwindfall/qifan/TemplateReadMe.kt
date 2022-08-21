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

fun createReadMe(githubContent: List<FeedItem>, codeTimeContent: List<FeedItem>, stravaContent: List<FeedItem>): String {
  return """
  <h2> Hi 👋, I'm Qifan ! </h2>
  <a href="https://github.com/underwindfall/iBeats"><img align="right" width="150px" src="https://raw.githubusercontent.com/underwindfall/iBeats/main/files/heart.svg"/></a>
  <p><em>Software engineer currently work @<a href="https://www.netatmo.com">Netatmo</a></em></p>
  <p><a href="https://qifanyang.com/resume" target="_blank"> 🔭 Know more about me</a> This is my personal resume site, I built it for fun.</p>
  <table width="960px"><tr><td valign="top" width="50%">

   #### 📷 Github Activity
   <!-- githubActivity starts -->
${githubContent.joinToString("\n\n") { "  $it" }}
   <!-- githubActivity ends -->
   </td><td valign="top" width="50%">

   #### 🌏 Something about me
   <!-- profile starts -->
   ```kotlin
   data class underwindfall(
        val pronouns: String = "he|him",
        val askMeAbout: List<String> = listOf(
          "Kotlin", "Java",
          "Dart","Javascript", "Typescript",
          "Swift"
        )
        val toLearn: () -> Unit = {
          "Flutter" to "For Fun",
          "Jetpack Compose" to "Future"
        }
        val dailyLife: Unit = (0..end).reduce { acc, new ->
           study(new)
           coding(new)
           sumUp(acc) + haveFun(new)
        }
   )
   ```
   <!-- profile ends -->
   </td></tr><tr><td valign="top" width="50%">
   
   #### 🏊‍♂️ <a href="https://gist.github.com/underwindfall/377ee88ba1fabd1e93516e48ca9c61eb" target="_blank">Weekly Development Breakdown</a>
    <!-- codeTime starts -->
    ```text
${codeTimeContent.joinToString("\n") { "$it" }}
    ```
    <!-- codeTime starts -->
    </td>
    <td valign="top" width="50%">

    #### 🤾‍♂️ <a href="https://gist.github.com/underwindfall/76198d6f6918f9f94d022c8ad881f98b" target="_blank">Recent Sports</a>

    <!-- Sports starts -->
    ```text
${stravaContent.joinToString("\n") { "$it" }}
    ```
    <!-- Sports ends -->
    </td></tr></table>
  """.trimIndent()
}
