rootProject.name = "thymeleaf-java-example"
includeBuild("..\\..\\thymeleaf"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-thymeleaf")).using(project(":"))
    }
}
includeBuild("..\\..\\core"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-core")).using(project(":"))
    }
}