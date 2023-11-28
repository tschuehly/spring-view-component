package de.tschuehly.spring.viewcomponent.core

import org.junit.jupiter.api.Test

abstract class JteIntegrationTestBase: IntegrationTestBase() {
    @Test
    override fun testNestedActionComponent() {
        val expectedHtml =
            //language=html
            """
                <html>
                <nav>This is a Navbar</nav>
                <body id="layoutviewcomponent">
                <div id="actionviewcomponent" style="display: contents;">
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
                </div>
                </body>
                <footer>This is a footer</footer>
                </html>
            """.trimIndent()
        assertEndpointReturns("/nested-action", expectedHtml)
    }

}