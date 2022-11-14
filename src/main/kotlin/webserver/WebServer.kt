package webserver

// write your web framework code here:

fun scheme(url: String): String = url.substringBefore(":")

fun host(url: String): String = url.substringAfter("://").substringBeforeLast("/")

fun path(url: String): String = url.substringAfter(host(url)).substringBefore("?")

fun queryParams(url: String): List<Pair<String, String>> {
  val allParams = url.substringAfter("?")
  return emptyList()
}

// http handlers for a particular website...

fun homePageHandler(request: Request): Response = Response(Status.OK, "This is Imperial.")
fun main() {
  println(scheme("https://www.google.com/search?q=kotlin&safe=active"))
  println(host("https://www.google.com/search?q=kotlin&safe=active"))
  println(path("https://www.google.com/search?q=kotlin&safe=active"))
}
