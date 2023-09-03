package de.tschuehly.example.jte;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ThymeleafJavaIntegrationTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void testIndexComponent() {
        var expectedHtml =
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
                        """.stripIndent();
        assertEndpointReturns("/", expectedHtml);
    }

    @Test
    void testSimpleComponent() {
        //language=html
        var expectedHtml = "<div>\n" +
                "  <h2>This is the SimpleViewComponent</h2>\n" +
                "  <div>Hello World</div>\n" +
                "</div>";
        assertEndpointReturns("/simple", expectedHtml);
    }

    @Test
    void testActionComponent() {
        var expectedHtml =
                //language=HTML
                """
                        <html xmlns="http://www.w3.org/1999/xhtml">
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
                         \s
                        </table>

                        <h2>ViewAction Put/Patch Person Form</h2>

                        <form style="display: inline-grid; gap: 0.5rem">
                          <label>
                            Name <input type="text" id="name" name="name" value="Thomas">
                          </label>
                          <label>
                            Age: <input type="number" id="age" name="age" value="23">
                          </label>
                          <label>
                            Location: <input type="text" id="location" name="location" value="Ludwigsburg">
                          </label>
                          <button type="submit" hx-put="/actionviewcomponent/savepersonput" hx-target="#actionviewcomponent">Save Changes using Put</button>
                          <button type="submit" hx-patch="/actionviewcomponent/savepersonpatch" hx-target="#actionviewcomponent">Save Changes using Patch</button>
                        </form>
                        </body>
                        </html>
                                        """;
        assertEndpointReturns(
                "/action",
                expectedHtml
        );
    }

    @Test
    void testNestedActionComponent() {
        var expectedHtml =
                //language=html
                """
                        <html>
                        <body id="layoutviewcomponent">
                        <nav>
                          This is the NavBar
                        </nav>
                        <div id="actionviewcomponent" data-nested-view-component>
                        <html xmlns="http://www.w3.org/1999/xhtml">
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
                         \s
                        </table>

                        <h2>ViewAction Put/Patch Person Form</h2>

                        <form style="display: inline-grid; gap: 0.5rem">
                          <label>
                            Name <input type="text" id="name" name="name" value="Thomas">
                          </label>
                          <label>
                            Age: <input type="number" id="age" name="age" value="23">
                          </label>
                          <label>
                            Location: <input type="text" id="location" name="location" value="Ludwigsburg">
                          </label>
                          <button type="submit" hx-put="/actionviewcomponent/savepersonput" hx-target="#actionviewcomponent">Save Changes using Put</button>
                          <button type="submit" hx-patch="/actionviewcomponent/savepersonpatch" hx-target="#actionviewcomponent">Save Changes using Patch</button>
                        </form>
                        </body>
                        </html>
                        </div>
                        <footer>
                          This is a footer
                        </footer>
                        </body>


                        </html>
                        """;
        assertEndpointReturns("/nested-action", expectedHtml);
    }

    @Test
    void testLayoutComponent() {
        //language=html
        var expectedHtml =
                """
                        <html>
                        <body id="layoutviewcomponent">
                        <nav>
                          This is the NavBar
                        </nav>
                        <div id="simpleviewcomponent" data-nested-view-component><div>
                          <h2>This is the SimpleViewComponent</h2>
                          <div>Hello World</div>
                        </div></div>
                        <footer>
                          This is a footer
                        </footer>
                        </body>
                        </html>
                        """.stripIndent();
        assertEndpointReturns("/layout", expectedHtml);
    }


    @Test
    void testResourceTemplate() {
        var expectedHtml = "Hello World";
        assertEndpointReturns("/resource-template", expectedHtml);
    }

    private void assertEndpointReturns(String url, String expectedHtml) {
        var response = this.testRestTemplate.exchange(
                url, HttpMethod.GET, null, String.class);
        Assertions.assertEquals(
                HttpStatus.OK, response.getStatusCode()
        );
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(
                rmWhitespaceBetweenHtmlTags(expectedHtml),
                rmWhitespaceBetweenHtmlTags(response.getBody())
        );
    }

    String rmWhitespaceBetweenHtmlTags(String html) {
        return html.replaceAll("(?<=>)(\\s*)(?=\\w)", "")
                .replaceAll("(?<=\\w)(\\s*)(?=<)", "")
                .replaceAll("(?<=>)(\\s*)(?=<)", "")
                .replaceAll("\r\n", "")
                .trim();
    }
}
