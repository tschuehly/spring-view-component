rootProject.name = "kte-example"

includeBuild("..\\..\\jte\\kte"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-kte")).using(project(":"))
    }
}
includeBuild("..\\..\\jte\\jte-compiler"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-jte-compiler")).using(project(":"))
    }
}
includeBuild("..\\..\\core"){
    dependencySubstitution {
        substitute(module("de.tschuehly:spring-view-component-core")).using(project(":"))
    }
}