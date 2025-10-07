# Beispielprojekt spring-vue-verknotung

Mini-Beispiel für automatischen Build "Vue-Frontend + Spring-Backend"
mit Paketierung in ausführbarem Jar.

Mit "./gradlew bootJar" wird

  - das Vue-Frontend im frontend-Ordner gebaut
  - in das Spring-Projekt als statischer Web-Kram kopiert
  - ein ausführbares jar-File in build/libs bereitgestellt

Ausführung mit 

   java -jar build/libs/springverknotung-0.0.1-SNAPSHOT.jar

Anwendung dann erreichbar unter http://localhost:8080

Vue-Frontend zeigt Button, der per REST die Zeit auf der Spring-Seite abfragt
und ausgibt.


build.gradle enthält zusätzliche Tasks:

# Gradle-Task "build_frontend"

  - führt "npm install" in Frontend-Verzeichnis aus, um Dependencies zu installieren
  - führt "npm run build" aus, um Frontend-Build in frontend/dist bereit zu stellen
  - löscht Zielverzeichnis src/main/resources/public
  - kopiert gebauten Frontend-Code aus frontend/dist in src/main/resources/public

Task wird als Dependency an Spring/Gradle "bootJar"-Standardtask gehängt und läuft
damit vor dem eigentlichen "bootJar", so das zu dessen Ausführung das Vue-Frontend
als statischer Inhalt in das ausführbare Spring-Jar übernommen wird.

Generiertes ausführbares Jar landet in build/libs/springverknotung-0.0.1-SNAPSHOT.jar



# Gradle-Task "clean_frontend"

  - löscht src/main/resources/public
  - löscht frontend/node_modules
  - löscht frontend/dist

Task hängt an Spring/Gradle "clean"-Standardtask. "./gradlew clean" entfernt
also alle generierten Inhalte aus dem Projekt.


