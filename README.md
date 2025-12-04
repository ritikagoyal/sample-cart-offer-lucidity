# Prerequisities
JDK 11
Docker

# How bring the mockservice up
cd mockserver  
docker compose up  
the mocke server will start at port 1080

# How bring the service up
./mvnw clean install -DskipTests  
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED -jar target/simple-springboot-app-0.0.1-SNAPSHOT.jar
The server will start at port 9001

# How to run the tests
./mvnw test  
