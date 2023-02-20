package de.tschuehly.thymeleafviewcomponent;

import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

public class ViewComponentTemplateEngineConfig {
    public static SpringTemplateEngine templateEngine(Class<?> clazz){
        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(templateResolver(clazz));
        return springTemplateEngine;
    }

    private static ITemplateResolver templateResolver(Class<?> clazz) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver(clazz.getClassLoader());
        resolver.setPrefix(clazz.getPackage().getName().replace(".","/") + "/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setOrder(1);
        return resolver;
    }
}
