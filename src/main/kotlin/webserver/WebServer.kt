package webserver

// write your web framework code here:

fun scheme(url: String): String = url.substringBefore(":")
fun host(url: String): String   = url.substringAfter("://").substringBefore("/")
fun path(url: String): String   = url.substringAfter(host(url)).substringBefore("?")

fun queryParams(url: String): List<Pair<String, String>> {
  return if (url.substringAfter(host(url)) == "") {
    emptyList()
  } else {
    val allParams = url.substringAfter("?").split("&")

    allParams.map { x -> x.split("=") }.map { y -> y.zipWithNext() }.flatten()
  }
}

// http handlers for a particular website...

fun route(request: Request): Response {
  val url = request.url
  return when (path(url)) {
      "/" -> homePageHandler(request)
      "/say-hello" -> helloHandler(request)
      "/computing" -> computingPageHandler(request)
      "/search" -> searchPageHandler(request)
      else -> errorHandler(request)
  }
}
fun homePageHandler(request: Request): Response      = Response(Status.OK, "This is Imperial.")
fun computingPageHandler(request: Request): Response = Response(Status.OK, "This is DoC.")
fun errorHandler(request: Request): Response         = Response(Status.NOT_FOUND, "404: PAGE NOT FOUND")
fun searchPageHandler(request: Request): Response    = Response(Status.OK, "What would you like to know about ICL?")

fun helloHandler(request: Request): Response {
  val url       = request.url
  val nameParam = queryParams(url).filter { t -> t.first == "name" }
  val style     = queryParams(url).filter { t -> t.first == "style" }

  // if there is a query parameter called "name"
  return if (nameParam.isEmpty()) {
    when {
      style.isEmpty()                      -> Response(Status.OK, "Hello, World!")
      style.first().second == "shouting"   -> Response(Status.OK, "HELLO, WORLD!")
      style.first().second == "whispering" -> Response(Status.OK, "hello, world")
      else                                 -> Response(Status.OK, "Invalid Response")
    }
  } else {
    val name      = nameParam.first().second
    val nameLower = name.lowercase()
    val nameUpper = name.uppercase()
    when {
      style.isEmpty()                      -> Response(Status.OK, "Hello, $name!")
      style.first().second == "shouting"   -> Response(Status.OK, "HELLO, $nameUpper!")
      style.first().second == "whispering" -> Response(Status.OK, "hello, $nameLower")
      else                                 -> Response(Status.OK, "Invalid Response")
    }
  }
}


fun main() {
  println(path("http://www.imperial.ac.uk/"))
}
