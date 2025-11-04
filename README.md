
## 🕹️ Projektbeschreibung

<img width="2487" height="1465" alt="Screenshot from 2025-10-27 11-49-13" src="https://github.com/user-attachments/assets/ae7f493d-8bb1-4dda-8ec9-3fc93d5e584f" />

**SnackMan Multiplayer** ist ein 3D-Mehrspieler-Spiel, in dem mehrere **SnackMen** und **Geister** von verschiedenen Spielern gesteuert werden.
Jeder Spieler erlebt das Spiel aus einer **3D-Ego-Perspektive**.

Zu Beginn sind die Wege mit Snacks unterschiedlicher Kalorienwerte belegt.
Die **SnackMen** sammeln Lebensmittel, um Kalorien aufzunehmen – abhängig vom **Nutri-Score** der gesammelten Snacks.
**Geister** jagen die SnackMen.

Zusätzlich gibt es **robotische Hühner**, die ebenfalls Snacks fressen und Eier legen. Diese Eier können von SnackMen gesammelt werden und liefern wertvolle Proteine sowie viele Kalorienpunkte beim Verzehr.

Darüber hinaus können weitere **sammel- und nutzbare Gegenstände** integriert werden, die zusätzliche Funktionen ermöglichen.
Die **Gewinnbedingung** besteht darin, einen bestimmten Mindest-Kalorienstand zu erreichen.

---

## 💻 Meine Rolle im Projekt

Ich war verantwortlich für den **Aufbau des Multiplayer-Lobbysystems** mit **Echtzeit-Synchronisation** zwischen den Spielern.
Spieler können **Lobbys erstellen, löschen, beitreten** und **Einladungslinks** an andere senden, um gemeinsam zu spielen.

<img width="2483" height="1466" alt="Screenshot from 2025-10-27 11-50-50" src="https://github.com/user-attachments/assets/d7c49fa1-3073-490b-8281-c11fd393ca73" />
<img width="2489" height="1458" alt="Screenshot from 2025-10-27 11-50-56" src="https://github.com/user-attachments/assets/427b91d8-d46b-448b-ae24-8f2870f85f91" />

Darüber hinaus habe ich eine zusätzliche **Map-Import- und Export-Funktion** entwickelt,
die es ermöglicht, **Karten vor Spielbeginn zu importieren** und **nach Spielende zu exportieren**.

<img width="2483" height="1457" alt="Screenshot from 2025-10-27 11-52-14" src="https://github.com/user-attachments/assets/3f4bec43-be86-4d4d-bb3d-1dff88e24ff0" />
<img width="2480" height="1466" alt="Screenshot from 2025-10-27 11-53-49" src="https://github.com/user-attachments/assets/a1c09bed-f822-4818-a946-6b6e94355071" />


---

## 🧩 Technologien verwendet

| Bereich           | Technologie                     |
| ----------------- | ------------------------------- |
| **Backend**       | Spring Boot (Gradle), WebSocket |
| **Datenbank**     | PostgreSQL                      |
| **Frontend**      | Vue.js, Three.js                |
| **Tools**         | npm, Gradlew                    |
| **Versionierung** | Git & GitHub                    |

---

## 🚀 Projekt starten

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


