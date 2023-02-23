package de.tschuehly.thymeleafviewcomponent

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractHttpMessageConverter
import org.springframework.stereotype.Component
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext

@Component
class ViewComponentConverter(
    val applicationContext: ApplicationContext
) :
    AbstractHttpMessageConverter<ViewComponentContext>(MediaType("text","viewcomponent")) {

    override fun supports(clazz: Class<*>): Boolean {
        return ViewComponentContext::class.java.isAssignableFrom(clazz)
    }

    override fun writeInternal(t: ViewComponentContext, outputMessage: HttpOutputMessage) {

        if(t.componentClass == null){
            throw Error("Could not get the ViewComponent Class")
        }
        try {
            outputMessage.body.also { outputStream ->
                val htmlContext = Context()
                htmlContext.setVariables(t.context)
                htmlContext.setVariable(
                    ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
                    ThymeleafEvaluationContext(applicationContext, null)
                )
                val engine = TemplateEngineConfig.templateEngine(t.componentClass)

                val htmlBody = engine
                    .process(
                        t.componentClass.simpleName.substringBefore("$$"),
                        htmlContext
                    )
                outputStream.write(htmlBody.toByteArray())
                outputStream.close()

            }
        }catch (e: Exception){
            println("Error")
            println(e)
        }
    }

    override fun readInternal(
        clazz: Class<out ViewComponentContext>,
        inputMessage: HttpInputMessage
    ): ViewComponentContext {
        TODO("Not yet implemented")
    }
}