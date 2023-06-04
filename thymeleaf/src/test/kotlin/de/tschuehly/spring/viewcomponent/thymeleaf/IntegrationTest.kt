package de.tschuehly.spring.viewcomponent.thymeleaf

import de.tschuehly.spring.viewcomponent.thymeleaf.application.ThymeleafTestApplication
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.util.StringUtils


@SpringBootTest(
    classes = [ThymeleafTestApplication::class]
)
@AutoConfigureMockMvc
class IntegrationTest(
    @Autowired val mockMvc: MockMvc
) {
    @Test
    fun testSimpleComponent() {
        val expectedHtml = """<div>HelloWorld</div>"""
        assertEndpointReturns("/simple",expectedHtml)
    }
    @Test
    fun testNestedComponent() {
        val expectedHtml = """<h1>NestedViewComponent</h1><div>HelloWorld</div>"""
        assertEndpointReturns("/nested",expectedHtml)
    }
    @Test
    fun testLayoutComponent() {
        val expectedHtml = """<nav>ThisisaNavbar</nav><div>HelloWorld</div><footer>Thisisafooter</footer>"""
        assertEndpointReturns("/layout",expectedHtml)
    }
    @Test
    fun testMultiComponent() {
        val expectedHtml = """<div>HelloWorld</div><nav>ThisisaNavbar</nav><div>HelloWorld</div><footer>Thisisafooter</footer>"""
        assertEndpointReturns("/multi",expectedHtml)

    }

    fun assertEndpointReturns(url: String, expectedHtml: String){
        mockMvc.get(url).andExpect {
            status { isOk() }
        }.andReturn().also {
            Assertions.assertEquals(expectedHtml,it.response.contentAsString.rmWhite())
        }
    }


    fun String.rmWhite() = StringUtils.trimAllWhitespace(this)

}