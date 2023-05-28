package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.jte.application.JteTestApplication
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@SpringBootTest(
    classes = [JteTestApplication::class]
)
@AutoConfigureMockMvc
class IntegrationTest(
    @Autowired val mockMvc: MockMvc
) {
    @Test
    fun testSimpleComponent() {
        val expectedHash = """<!DOCTYPE html>
<div>Hello World</div>
</html>""".hashCode()
        val result = mockMvc.get("/simpleComponent").andExpect {
            status { isOk() }
            content {

                // Assertions.assertEquals(hashCode(),expectedHash)
            }
        }.andReturn().also {
            println(it.response.contentAsString)
        }
    }
}