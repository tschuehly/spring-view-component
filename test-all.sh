./gradlew :core:publishToMavenLocal :thymeleaf:publishToMavenLocal :jte:publishToMavenLocal :jte-compiler:publishToMavenLocal

echo "Testing thymeleaf-kotlin-example"
cd ./examples/thymeleaf-kotlin-example
./gradlew clean test


cd ../thymeleaf-java-example
echo "Testing thymeleaf-java-example with mvnw"
./mvnw clean test
echo "Testing thymeleaf-java-example with gradlew"
./gradlew clean test
cd ../kte-example
echo "Testing kte-example"
./gradlew clean test

cd ../jte-example
echo "Testing jte-example with mvnw"
./mvnw clean test
echo "Testing jte-example with gradlew"
./gradlew clean test