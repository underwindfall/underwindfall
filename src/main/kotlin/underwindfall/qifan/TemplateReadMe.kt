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

fun createReadMe(githubContent: List<FeedItem>, codeTimeContent: List<FeedItem>): String {
  return """
  <h2> Hi 👋, I'm Qifan ! </h2>
  <p><em>Software engineer currently work @<a href="https://www.netatmo.com">Netatmo</a>
  </em></p><p><a href="https://qifanyang.com/resume" target="_blank"> 🔭 Know more about me</a> This is my personal resume site, I built it for fun.</p>
  <table><tr><td valign="top" rowspan="2">

   ## 📷 Github Activity
   <!-- githubActivity starts -->
${githubContent.joinToString("\n\n") { "    $it" }}
   <!-- githubActivity ends -->
   </td><td valign="top">

   ## 🌏 Something about me
   <!-- profile starts -->
   <a href="https://github.com/underwindfall" width="100%">
     <img src="https://activity-graph.herokuapp.com/graph?username=underwindfall&theme=react-dark&hide_border=true&bg_color=00000000&color=BDDFFF&line=6E93B5&point=BDDFFF"/>
   </a>
   <br/>
   <br/>
   <br/>

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
   </td></tr><tr><td valign="top">

   ## 🏊‍♂️ <a href="https://gist.github.com/underwindfall/377ee88ba1fabd1e93516e48ca9c61eb" target="_blank">Weekly Development Breakdown</a>
    <!-- codeTime starts -->
    ```text
${codeTimeContent.joinToString("\n") { "$it" }}
    ```
    <!-- codeTime starts -->
    </td></tr></table>
  """.trimIndent()
}
