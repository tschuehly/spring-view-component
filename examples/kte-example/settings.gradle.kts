rootProject.name = "kte-example"


includeBuild("..\\..\\kte"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-kte")).using(project(":"))
    }
}
includeBuild("..\\..\\core"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-core")).using(project(":"))
    }
}
