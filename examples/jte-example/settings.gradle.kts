rootProject.name = "jte-example"

includeBuild("..\\..\\jte\\jte"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-jte")).using(project(":"))
    }
}
includeBuild("..\\..\\core"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-core")).using(project(":"))
    }
}