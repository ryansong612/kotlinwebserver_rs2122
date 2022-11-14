package webserver

import org.junit.Test
import kotlin.test.assertEquals

class WebServerTest {

  @Test
  fun `can extract scheme`() {
    assertEquals("http", scheme("http://www.imperial.ac.uk/"))
    assertEquals("https", scheme("https://www.imperial.ac.uk/"))
    assertEquals("http", scheme("http://www.google.com/search?q=kotin"))
    assertEquals("https", scheme("https://www.google.com/search?q=kotin&safe=active"))
    assertEquals("https", scheme("https://ipv6.google.com"))
  }

  @Test
  fun `can extract host`() {
    assertEquals("www.imperial.ac.uk", host("http://www.imperial.ac.uk/"))
    assertEquals("www.imperial.ac.uk", host("https://www.imperial.ac.uk/"))
    assertEquals("www.imperial.ac.uk", host("https://www.imperial.ac.uk/computing"))
    assertEquals("www.imperial.ac.uk", host("https://www.imperial.ac.uk/computing/programming"))
    assertEquals("www.google.com", host("https://www.google.com/search?q=kotin&safe=active"))
    assertEquals("www.google.com", host("http://www.google.com/search?q=kotin"))
    assertEquals("www.baidu.com", host("https://www.baidu.com/"))
  }

  @Test
  fun `can extract path`() {
    assertEquals("/", path("http://www.imperial.ac.uk/"))
    assertEquals("/", path("https://www.imperial.ac.uk/"))
    assertEquals("/computing", path("https://www.imperial.ac.uk/computing"))
    assertEquals("/computing/programming", path("https://www.imperial.ac.uk/computing/programming"))
    assertEquals("/computing", path("https://www.imperial.ac.uk/computing?q=abc"))
    assertEquals("/search", path("http://www.google.com/search?q=kotin"))
    assertEquals("/computing/programming/exam-marks", path("https://imperial.ac.uk/computing/programming/exam-marks"))
  }

  @Test
  fun `can extract query params`() {
    assertEquals(listOf(Pair("q", "xxx")), queryParams("http://www.imperial.ac.uk/?q=xxx"))
    assertEquals(listOf(Pair("q", "xxx"), Pair("rr", "zzz")), queryParams("http://www.imperial.ac.uk/?q=xxx&rr=zzz"))
    assertEquals(listOf(Pair("q", "kotlin"), Pair("safe","active")), queryParams("https://www.google.com/search?q=kotlin&safe=active"))
  }

  @Test
  fun `when no query params in url, empty list is extracted`() {
    assertEquals(listOf(), queryParams("http://www.imperial.ac.uk/"))
  }


  @Test
  fun `says hello world`() {
    val request = Request("http://www.imperial.ac.uk/say-hello")
    assertEquals("Hello, World!", helloHandler(request).body)
  }

  @Test
  fun `can be customised with particular name`() {
    val request = Request("http://www.imperial.ac.uk/say-hello?name=Fred")
    assertEquals("Hello, Fred!", helloHandler(request).body)
  }

  @Test
  fun `can process multiple params`() {
    val request = Request("http://www.imperial.ac.uk/say-hello?name=Fred&style=shouting")
    assertEquals("HELLO, FRED!", helloHandler(request).body)
  }

// ***** Tests for Routing *****

  @Test
  fun `can route to hello handler`() {
    val request = Request("http://www.imperial.ac.uk/say-hello?name=Fred")
    assertEquals("Hello, Fred!", route(request).body)
  }

  @Test
  fun `can route to homepage handler`() {
    assertEquals("This is Imperial.", route(Request("http://www.imperial.ac.uk/")).body)
    assertEquals("This is DoC.", route(Request("http://www.imperial.ac.uk/computing")).body)
  }

  @Test
  fun `gives 404 when no matching route`() {
    assertEquals(Status.NOT_FOUND, route(Request("http://www.imperial.ac.uk/not-here")).status)
  }

//  ***** Tests for the Extensions *****

// *** More flexible routing ***

  @Test
  fun `calling configureRoutes() returns app which can handle requests`() {

    val app = configureRoutes(mapOf(Pair("/", ::homePageHandler), Pair("/say-hello", ::helloHandler),
              Pair("/computing", ::computingPageHandler)))

    assertEquals("This is Imperial.", app(Request("http://www.imperial.ac.uk/")).body)
    assertEquals("This is DoC.", app(Request("http://www.imperial.ac.uk/computing")).body)
  }

  // *** Filters ***

  @Test
  fun `filter prevents access to protected resources `() {

    val app = configureRoutes(mapOf(Pair("/", ::homePageHandler), Pair("/say-hello", ::helloHandler),
      Pair("/computing", ::computingPageHandler),
      Pair("/exam-marks", requireToken("password1", ::examMarksHandler))))

    val request = Request("http://www.imperial.ac.uk/exam-marks")
    assertEquals(Status.FORBIDDEN, app(request).status)
  }

  @Test
  fun `filter allows access to protected resources with token`() {

    val app = configureRoutes(mapOf(Pair("/", ::homePageHandler), Pair("/say-hello", ::helloHandler),
      Pair("/computing", ::computingPageHandler),
      Pair("/exam-marks", requireToken("password1", ::examMarksHandler))))

    val request = Request("http://www.imperial.ac.uk/exam-marks", "password1")
    assertEquals(Status.OK, app(request).status)
    assertEquals("This is very secret.", app(request).body)
  }
}
