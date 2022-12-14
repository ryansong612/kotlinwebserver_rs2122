package webserver

// write your web framework code here:
typealias HttpHandler = (Request) -> Response

fun scheme(url: String): String =
  url.substringBefore(":")
fun host(url: String): String =
  url.substringAfter("://").substringBefore("/")
fun path(url: String): String =
  url.substringAfter(host(url)).substringBefore("?")

fun queryParams(url: String): List<Pair<String, String>> {
  return if (url.substringAfter(host(url)) == "") {
    emptyList()
  } else {
    val allParams = url.substringAfter("?").split("&")
    allParams.map { x -> x.split("=") }
      .map { y -> y.zipWithNext() }
      .flatten()
  }
}
// http handlers for a particular website...

val mapping = mapOf(
  ("/" to ::homePageHandler),
  ("/say-hello" to ::helloHandler),
  ("/computing" to ::computingPageHandler),
  (
    "/exam-marks" to requireToken("password1", ::examMarksHandler)
    )
)

fun route(request: Request): Response = configureRoutes(mapping).invoke(request)
//  val url = request.url
//  return when (path(url)) {
//    "/"          -> homePageHandler(request)
//    "/say-hello" -> helloHandler(request)
//    "/computing" -> computingPageHandler(request)
//    else         -> errorHandler(request)
//  }

// Handlers for different routing options
fun homePageHandler(request: Request): Response =
  Response(Status.OK, "This is Imperial.")
fun computingPageHandler(request: Request): Response =
  Response(Status.OK, "This is DoC.")
fun errorHandler(request: Request): Response =
  Response(Status.NOT_FOUND, "404: PAGE NOT FOUND")

fun helloHandler(request: Request): Response {
  val paramHandlers = mapOf(
    Pair("name", ::nameHandler),
    Pair("style", ::styleHandler)
  )
  val params = queryParams(request.url)
  var hello = "Hello, World!"
  for (param in params) {
    hello = paramHandlers[param.first]!!.invoke(hello, param.second)
  }
  return Response(Status.OK, hello)
}

// if the query parameter had nane in there
fun nameHandler(name: String, param: String): String =
  "Hello, $param!"

// if the query parameter had style in there
fun styleHandler(msg: String, param: String): String {
  return when (param) {
    "shouting"   -> msg.uppercase()
    "whispering" -> msg.lowercase()
    else         -> msg
  }
}

fun configureRoutes(routeMap: Map<String, HttpHandler>): HttpHandler =
  { requests ->
    routeMap.getOrDefault(path(requests.url), ::errorHandler)
      .invoke(requests)
  }

fun requireToken(token: String, wrapped: HttpHandler): HttpHandler {
  return { request ->
    // checks for the case where token is valid
    if (request.authToken == token) {
      wrapped.invoke(request)
    } else {
      Response(Status.FORBIDDEN, "INVALID AUTHORIZATION TOKEN PASSED")
    }
  }
}

fun examMarksHandler(request: Request): Response =
  Response(Status.OK, "This is very secret.")

fun main() {
  println(path("http://www.imperial.ac.uk/"))
}
