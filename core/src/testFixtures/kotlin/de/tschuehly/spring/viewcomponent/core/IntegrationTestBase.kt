package de.tschuehly.spring.viewcomponent.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
abstract class IntegrationTestBase {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate
    @Test
    fun testIndexComponent() {
        val expectedHtml =
            //language=html
            """
                <html>
                    <head><script src="http://localhost:35729/livereload.js"></script></head>
                    <div>
                        <h1>This is the IndexViewComponent</h1>
                    </div>
                    <a href="/">IndexViewComponent</a><br>
                    <a href="/simple">SimpleViewComponent</a><br>
                    <a href="/layout">LayoutViewComponent</a><br>
                </html>
            """.trimIndent()
        assertEndpointReturns("/", expectedHtml)

    }

    @Test
    fun testSimpleComponent() {
        //language=html
        val expectedHtml = """
            <div>
                <h2>This is the SimpleViewComponent</h2>
                <div>Hello World</div>
            </div>
        """.trimIndent()
        assertEndpointReturns("/simple", expectedHtml)
    }

    @Test
    fun testLayoutComponent() {
        //language=html
        val expectedHtml =
            """
                <html>
                <nav>This is a Navbar</nav>
                <body>
                <div><h2>This is the SimpleViewComponent</h2>
                <div>Hello World</div></div></body>
                <footer>This is a footer</footer></html>
                            """.trimIndent()
        assertEndpointReturns("/layout", expectedHtml)
    }

    fun assertEndpointReturns(url: String, expectedHtml: String) {
        val response: ResponseEntity<String> = this.testRestTemplate
            .exchange(url, HttpMethod.GET, null, String::class.java)
        assertThat(response.statusCode)
            .isEqualTo(HttpStatus.OK)
        Assertions.assertEquals(
            expectedHtml.rmWhitespaceBetweenHtmlTags(), response.body?.rmWhitespaceBetweenHtmlTags()
        )
    }

    fun String.rmWhitespaceBetweenHtmlTags(): String {
        // Replace whitespace between > and word
        return this.replace("(?<=>)(\\s*)(?=\\w)".toRegex(), "")
            .replace("(?<=\\w)(\\s*)(?=<)".toRegex(), "")
            .replace("(?<=>)(\\s*)(?=<)".toRegex(), "")
            .replace("\r\n","\n")
            .trim()
    }
}