rootProject.name = "kte-example"

includeBuild("..\\..\\jte"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-jte")).using(project(":"))
    }
}
includeBuild("..\\..\\core"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-core")).using(project(":"))
    }
}