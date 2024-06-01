rootProject.name = "thymeleaf-kotlin-example"

includeBuild("..\\..\\thymeleaf"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-thymeleaf")).using(project(":"))
    }
}
