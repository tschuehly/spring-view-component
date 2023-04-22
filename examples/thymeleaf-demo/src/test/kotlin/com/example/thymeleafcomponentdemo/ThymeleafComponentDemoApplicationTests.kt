package com.example.thymeleafcomponentdemo

import com.example.thymeleafcomponentdemo.web.home.HomeViewComponent
import com.example.thymeleafcomponentdemo.web.navigation.NavigationViewComponent
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThymeleafComponentDemoApplicationTests {

    @Autowired
    lateinit var homeViewComponent: HomeViewComponent
    @Autowired
    lateinit var navigationViewComponent: NavigationViewComponent
    val restTemplate = TestRestTemplate()

    @Value("\${local.server.port}")
    lateinit var port: Number

    @Test
    fun testHomeRender(){
        homeViewComponent.render().also {
            println(it.toString())
        }
    }

    @Test
    fun testNavRender(){
        navigationViewComponent.render().also {
            println(it.toString())
        }
    }
    @Test
    fun getIndexPage(){
        restTemplate.getForEntity("http://localhost:${port}/",String::class.java).also {
            println(it.toString())
        }
    }
}
