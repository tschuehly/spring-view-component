./gradlew :core:publishToMavenLocal :thymeleaf:publishToMavenLocal :jte:publishToMavenLocal :jte-compiler:publishToMavenLocal

cd ./examples/thymeleaf-kotlin-example
./gradlew test


../thymeleaf-java-example
./mvnw test
./gradlew test

cd ../kte-example
./gradlew test

cd ../jte-example
./mvnw test
./gradlew test