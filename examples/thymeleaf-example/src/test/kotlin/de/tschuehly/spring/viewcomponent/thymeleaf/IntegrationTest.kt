package de.tschuehly.spring.viewcomponent.thymeleaf

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.Assert
import org.springframework.util.StringUtils


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class IntegrationTest(
    @Autowired val testRestTemplate: TestRestTemplate
) {
    @Test
    fun testIndexComponent() {
        val expectedHtml =
            """<html><div><h1>This is the IndexViewComponent</h1></div><a href="/">IndexViewComponent</a><br><a href="/simple">SimpleViewComponent</a><br><a href="/layout">LayoutViewComponent</a><br><a href="/action">ActionViewComponent</a><br><a href="/nested-action">NestedActionViewComponent</a><br></br></br></br></br></br></html>"""
        assertEndpointReturns("/", expectedHtml)
    }

    @Test
    fun testSimpleComponent() {
        val expectedHtml = """<div><h2>This is the SimpleViewComponent</h2><div>Hello World</div></div>"""
        assertEndpointReturns("/simple", expectedHtml)
    }

    @Test
    fun testActionComponent() {
        assertEndpointReturns(
            "/action",
            """<html><head><script defer="" src="/webjars/htmx.org/dist/htmx.min.js"></script></head><body id="actionviewcomponent"><h2>ViewAction Get CountUp</h2><button hx-get="/custompath/countup" hx-target="#actionviewcomponent" hx-swap="outerHTML">Default ViewAction [GET]</button><h3>0</h3><h2>ViewAction Post AddItem</h2><form hx-post="/actionviewcomponent/additem" hx-target="#actionviewcomponent" hx-swap="outerHTML"><input type="text" name="item"><button type="submit">Save Item</button></input></form><table><tr><th>Item</th><th>Action</th></tr></table><h2>ViewAction Put/Patch Person Form</h2><form style="display: inline-grid; gap: 0.5rem"><label>Name<input type="text" id="name" name="name" value="Thomas"></input></label><label>Age: <input type="number" id="age" name="age" value="23"></input></label><label>Location: <input type="text" id="location" name="location" value="Ludwigsburg"></input></label><button type="submit" hx-put="/actionviewcomponent/savepersonput" hx-target="#actionviewcomponent" hx-swap="outerHTML">Save Changes using Put</button><button type="submit" hx-patch="/actionviewcomponent/savepersonpatch" hx-target="#actionviewcomponent" hx-swap="outerHTML">Save Changes using Patch</button></form></body></html>"""
        )
    }

    @Test
    fun testNestedActionComponent() {
        val expectedHtml =
            """<html><nav>This is a Navbar</nav><body><div id="actionviewcomponent" nestedviewcomponent=""><html><head><script defer="" src="/webjars/htmx.org/dist/htmx.min.js"></script></head><body><h2>ViewAction Get CountUp</h2><button hx-get="/custompath/countup" hx-target="#actionviewcomponent" hx-swap="outerHTML">Default ViewAction [GET]</button><h3>0</h3><h2>ViewAction Post AddItem</h2><form hx-post="/actionviewcomponent/additem" hx-target="#actionviewcomponent" hx-swap="outerHTML"><input type="text" name="item"><button type="submit">Save Item</button></input></form><table><tr><th>Item</th><th>Action</th></tr></table><h2>ViewAction Put/Patch Person Form</h2><form style="display: inline-grid; gap: 0.5rem"><label>Name<input type="text" id="name" name="name" value="Thomas"></input></label><label>Age: <input type="number" id="age" name="age" value="23"></input></label><label>Location: <input type="text" id="location" name="location" value="Ludwigsburg"></input></label><button type="submit" hx-put="/actionviewcomponent/savepersonput" hx-target="#actionviewcomponent" hx-swap="outerHTML">Save Changes using Put</button><button type="submit" hx-patch="/actionviewcomponent/savepersonpatch" hx-target="#actionviewcomponent" hx-swap="outerHTML">Save Changes using Patch</button></form></body></html></div></body><footer>This is a footer</footer></html>"""
        assertEndpointReturns("/nested-action", expectedHtml)
    }

    @Test
    fun testLayoutComponent() {
        val expectedHtml =
            """<html><nav>This is a Navbar</nav><body><div id="simpleviewcomponent" nestedviewcomponent=""><div><h2>This is the SimpleViewComponent</h2><div>Hello World</div></div></div></body><footer>This is a footer</footer></html>"""
        assertEndpointReturns("/layout", expectedHtml)
    }

    @Test
    fun testMultiComponent() {
        val expectedHtml =
            """<div><h2>This is the SimpleViewComponent</h2><div>Hello World</div></div><html><nav>This is a Navbar</nav><body><div id="simpleviewcomponent" nestedviewcomponent=""><div><h2>This is the SimpleViewComponent</h2><div>Hello World</div></div></div></body><footer>This is a footer</footer></html>"""
        assertEndpointReturns("/multi", expectedHtml)

    }

    fun assertEndpointReturns(url: String, expectedHtml: String) {
        val response: ResponseEntity<String> = this.testRestTemplate
            .exchange(url, HttpMethod.GET, null, String::class.java)
        assertThat(response.statusCode)
            .isEqualTo(HttpStatus.OK)
        Assertions.assertEquals(
            expectedHtml,response.body?.rmWhitespaceBetweenHtmlTags()
        )
    }

    fun String.rmWhitespaceBetweenHtmlTags(): String {
        // Replace whitespace between > and word
        return this.replace("(?<=>)(\\s*)(?=\\w)".toRegex(), "").run {
            // Replace whitespace between word and <
            this.replace("(?<=\\w)(\\s*)(?=<)".toRegex(), "")
        }.run {
            // Replace whitespace between > and <
            this.replace("(?<=>)(\\s*)(?=<)".toRegex(), "")
        }
    }

    fun String.rmWhite() = StringUtils.trimAllWhitespace(this)

}