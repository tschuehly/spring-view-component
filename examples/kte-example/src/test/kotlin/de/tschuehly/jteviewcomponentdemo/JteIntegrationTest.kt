package de.tschuehly.jteviewcomponentdemo


import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class JteIntegrationTest(
    @Autowired val testRestTemplate: TestRestTemplate
) {
    @Test
    fun testIndexComponent() {
        val expectedHtml =
            //language=html
            """
                <html>
                    <div>
                        <h1>This is the IndexViewComponent</h1>
                    </div>
                    <a href="/">IndexViewComponent</a><br>
                    <a href="/simple">SimpleViewComponent</a><br>
                    <a href="/layout">LayoutViewComponent</a><br>
                    <a href="/action">ActionViewComponent</a><br>
                    <a href="/nested-action">NestedActionViewComponent</a><br>
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
    fun testActionComponent() {
        val expectedHtml =
            //language=html
            """
                <html>
                <head>
                    <script src="http://localhost:35729/livereload.js"></script>
                    <script defer src="/webjars/htmx.org/dist/htmx.min.js"></script>
                </head>
                <body id="actionviewcomponent">
                <h2>ViewAction Get CountUp</h2>
                <button hx-get="/custompath/countup" hx-target="#actionviewcomponent">Default ViewAction [GET]</button>
                <h3>0</h3>
                <h2>ViewAction Post AddItem</h2>
                <form hx-post="/actionviewcomponent/additem" hx-target="#actionviewcomponent">
                    <input type="text" name="item">
                    <button type="submit">Save Item</button>
                </form>
                <table>
                    <tr>
                        <th>Item</th>
                        <th>Action</th>
                    </tr>
                </table>
                <h2>ViewAction Put/Patch Person Form</h2>
                <form style="display: inline-grid; gap: 0.5rem">
                    <label>Name<input type="text" id="name" name="name" value="Thomas">
                    </label>
                    <label>Age: <input type="number" id="age" name="age" value="23">
                    </label>
                    <label>Location: <input type="text" id="location" name="location" value="Ludwigsburg">
                    </label>
                    <button type="submit" hx-put="/actionviewcomponent/savepersonput" hx-target="#actionviewcomponent">
                        Save Changes using Put
                    </button>
                    <button type="submit" hx-patch="/actionviewcomponent/savepersonpatch" hx-target="#actionviewcomponent">
                        Save Changes using Patch
                    </button>
                </form>
                </body>
                </html>
            """.trimIndent()
        assertEndpointReturns(
            "/action",
            expectedHtml
        )
    }


    @Test
    fun testNestedActionComponent() {
        val expectedHtml =
            //language=html
            """
                <html>
                <nav>This is a Navbar</nav>
                <body id="layoutviewcomponent">
                <html>
                <head>
                    <script src="http://localhost:35729/livereload.js"></script>
                    <script defer src="/webjars/htmx.org/dist/htmx.min.js"></script>
                </head>
                <body id="actionviewcomponent">
                <h2>ViewAction Get CountUp</h2>
                <button hx-get="/custompath/countup" hx-target="#actionviewcomponent">Default ViewAction [GET]</button>
                <h3>0</h3>
                <h2>ViewAction Post AddItem</h2>
                <form hx-post="/actionviewcomponent/additem" hx-target="#actionviewcomponent">
                    <input type="text" name="item">
                    <button type="submit">Save Item</button>
                </form>
                <table>
                    <tr>
                        <th>Item</th>
                        <th>Action</th>
                    </tr>
                </table>
                <h2>ViewAction Put/Patch Person Form</h2>
                <form style="display: inline-grid; gap: 0.5rem">
                    <label>Name<input type="text" id="name" name="name" value="Thomas">
                    </label>
                    <label>Age: <input type="number" id="age" name="age" value="23">
                    </label>
                    <label>Location: <input type="text" id="location" name="location" value="Ludwigsburg">
                    </label>
                    <button type="submit" hx-put="/actionviewcomponent/savepersonput" hx-target="#actionviewcomponent">
                        Save Changes using Put
                    </button>
                    <button type="submit" hx-patch="/actionviewcomponent/savepersonpatch" hx-target="#actionviewcomponent">
                        Save Changes using Patch
                    </button>
                </form>
                </body>
                </html>
                </body>
                <footer>This is a footer</footer>
                </html>
            """.trimIndent()
        assertEndpointReturns("/nested-action", expectedHtml)
    }

    @Test
    fun testLayoutComponent() {
        //language=html
        val expectedHtml =
            """
                <html><nav>This is a Navbar</nav><body id="layoutviewcomponent"><div><h2>This is the SimpleViewComponent</h2><div>Hello World</div></div></body><footer>This is a footer</footer></html>
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