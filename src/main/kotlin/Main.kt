import java.net.URL
import org.jsoup.Jsoup
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JScrollBar
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists

fun ReadYamlFromUrl(url: String, charset: String): String {
    val content = URL(url).readText(charset(charset))
    return content
}

fun GetContentWithinRange(
    content_list: List<String>,
    start_str: String,
    end_str: String): Pair<Int, Int>{
    var start = 0
    var end = content_list.size

    for ((index, line) in content_list.withIndex()){
        if (line == start_str) {
            start = index
        } else if (line == end_str) {
            end = index
        }
    }

    return Pair(start, end)
}

fun ParseClash(url: String): Pair<List<String>, List<String>> {
    val charset = "UTF-8"
    val content = ReadYamlFromUrl(url, charset)

    val content_list = content.split("\n")

    var start_str: String
    var end_str: String
    var result: Pair<Int, Int>
    var start: Int
    var end: Int

    start_str = "proxies:"
    end_str = "proxy-groups:"
    result =
        GetContentWithinRange(content_list, start_str, end_str)
    start = result.first
    end = result.second
    val proxies = content_list.subList(start + 1, end)

    start_str = "  - name: \uD83D\uDE80 节点选择"
    end_str = "  - name: ♻️ 自动选择"
    result =
        GetContentWithinRange(content_list, start_str, end_str)
    start = result.first
    end = result.second
    val proxies_select = content_list.subList(start + 5, end)

    return Pair(proxies, proxies_select)
}

fun main() {
    val rawprefix = "https://raw.githubusercontent.com"

    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd")
    val formatted = current.format(formatter)
    println("当前日期和时间为: $formatted")

    val data =
        Jsoup.connect("https://github.com/changfengoss/" +
                "pub/tree/main/data").timeout(0).get()
    val times = data.getElementsByTag("a")

    var todaylink = ""

    for (time in times) {
        if (time.text() == formatted) {
            todaylink = time.attr("abs:href")
        }
    }

    val todayclashs =
        Jsoup.connect(todaylink).timeout(0).get()
    val clashs = todayclashs.getElementsByTag("a")

    val main_proxies = ArrayList<String>()
    val main_proxies_select = ArrayList<String>()

    for (clash in clashs) {
        if (clash.text().contains(".yaml")) {
            val url = rawprefix +
                    clash.attr("href")
                        .replace("/blob", "")

            val (proxies, proxies_select) = ParseClash(url)
            main_proxies.addAll(proxies)
            main_proxies_select.addAll(proxies_select)
        }
    }

    var proxies = "proxies:\n"
    for (proxy in main_proxies) {
        proxies = proxies + proxy + "\n"
    }
    println(proxies)

    val currentPath = Paths.get(System.getProperty("user.dir"))
    val filepath = currentPath.toString() + "/v2rayse.yaml"

    Path(filepath).deleteIfExists()
    File(filepath).bufferedWriter().use { out -> out.write(proxies) }
}
//fun main(args: Array<String>) {
//    val url = "https://raw.githubusercontent.com/" +
//                "changfengoss/pub/main/data/" +
//                "2022_10_13/V09Ae7.yaml"
//    val charset = "UTF-8"
//    val content = ReadYamlFromUrl(url, charset)
//
//    val content_list = content.split("\n")
//
//    var start_str: String
//    var end_str: String
//
//    start_str = "proxies:"
//    end_str = "proxy-groups:"
//    var result: Pair<Int, Int>
//    var start: Int
//    var end: Int
//
//    result =
//        GetContentWithinRange(content_list, start_str, end_str)
//    start = result.first
//    end = result.second
//    val proxies = content_list.subList(start + 1, end)
//    for (line in proxies) {
//        println(line)
//    }
//
//
//    start_str = "  - name: \uD83D\uDE80 节点选择"
//    end_str = "  - name: ♻️ 自动选择"
//    result =
//        GetContentWithinRange(content_list, start_str, end_str)
//    start = result.first
//    end = result.second
//    val proxies_selct = content_list.subList(start + 5, end)
//}
