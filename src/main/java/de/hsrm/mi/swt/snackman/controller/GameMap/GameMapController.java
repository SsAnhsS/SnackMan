package de.hsrm.mi.swt.snackman.controller.GameMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.lobby.Lobby;
import de.hsrm.mi.swt.snackman.services.LobbyManagerService;

/**
 * REST Controller for handling map-related API requests
 * This controller provides endpoints for retrieving game map data
 */
@RestController
// enable Cross-Origin Resource Sharing (CORS) for requests coming from the specified origin
@RequestMapping("/api")
public class GameMapController {

    Logger log = LoggerFactory.getLogger(GameMapController.class);
    @Autowired
    private LobbyManagerService lobbyManagerService;

    @GetMapping("/lobby/{lobbyId}/game-map")
    public ResponseEntity<GameMapDTO> getGameMap(@PathVariable("lobbyId") String lobbyId) {
        log.debug("Get GameMap");
        return ResponseEntity.ok(GameMapDTO.fromGameMap(lobbyManagerService.getGameMapByLobbyId(lobbyId)));
    }

    /**
     * Downloads the last map file as "SnackManMap.txt".
     *
     * @return ResponseEntity containing the map file as a resource, or:
     * - HTTP 404 (Not Found) if the file does not exist.
     * - HTTP 409 (Conflict) if an error occurs during the process.
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadMap(@RequestParam("lobbyId") String lobbyId) {
        try {
            String fileName = String.format("LastMap_%s.txt", lobbyId);
            Path filePath = Paths.get("./extensions/map/" + fileName).toAbsolutePath();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"SnackManMap.txt\"").body(resource);
        } catch (Exception e) {
            log.error("Error occurred: ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    /**
     * Upload custom map and save in folder "./extensions/map"
     *
     * @param file    uploaded File
     * @param lobbyId lobbyId, where upload custom map
     * @return ResponseEntity: file is uploaded sucessfully or
     * 400 (Bad Request) file is not valid
     * 409 (Conflict) status of an error occurs during the upload process
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadMap(@RequestParam("file") MultipartFile file, @RequestParam("lobbyId") String lobbyId) {
        try {
            if (!file.getOriginalFilename().endsWith(".txt")) {
                return ResponseEntity.badRequest().body("lobby.mapFile.invalidFileType");
            }

            // Check File-Content
            String fileContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            String validPattern = "^[SGCo#\\s]*$";

            if (!fileContent.matches(validPattern)) {
                return ResponseEntity.badRequest().body(
                        "lobby.mapFile.invalidChars"
                );
            }

            // Check the number of Position for Snackman and Ghost
            long countS = fileContent.chars().filter(ch -> ch == 'S').count();
            long countG = fileContent.chars().filter(ch -> ch == 'G').count();

            if (countS != 1) {
                return ResponseEntity.badRequest().body("lobby.mapFile.exactlyOneS");
            }

            if (countG < 4) {
                return ResponseEntity.badRequest().body("lobby.mapFile.ghostCount");
            }

            // Save File
            Path uploadPath = Paths.get("./extensions/map").toAbsolutePath();
            String fileName = String.format("SnackManMap_%s.txt", lobbyId);
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            log.error("Error occurred: ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    /**
     * Deletes the map file associated with a given lobby ID.
     * This method is invoked when a lobby no longer exists, and the map file
     * for that lobby needs to be deleted from the server.
     *
     * @param lobbyId
     * @return Returns an HTTP status code:
     * - HTTP 200 (OK) if the file was successfully deleted.
     * - HTTP 404 (Not Found) if the file does not exist.
     * - HTTP 500 (Internal Server Error) if an error occurs during the deletion process.
     */
    @DeleteMapping("/deleteMap")
    public ResponseEntity<Void> deleteUploadedMap(@RequestParam("lobbyId") String lobbyId) {
        try {
            String customMapName = String.format("SnackManMap_%s.txt", lobbyId);
            Path customMapPath = Paths.get("./extensions/map/" + customMapName).toAbsolutePath();

            String lastMapName = String.format("LastMap_%s.txt", lobbyId);
            Path lastMapPath = Paths.get("./extensions/map/" + lastMapName).toAbsolutePath();

            if (Files.exists(customMapPath) || Files.exists(lastMapPath)) {
                Files.delete(customMapPath);
                Files.delete(lastMapPath);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (IOException e) {
            log.error("Error occurred while deleting the file: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates the custom map usage status for a lobby.
     *
     * @param requestBody A Map containing the following parameters:
     *                    - lobbyId          :The ID of the lobby (String).
     *                    - usedCustomMap    :The custom map usage status (Boolean).
     * @return Returns an HTTP status code:
     * - 200 OK if the update is successful.
     * - 404 Not Found if the lobby is not found.
     * - 409 (Conflict) status of an error occurs during the upload process.
     */
    @PostMapping("/change-used-map-status")
    public ResponseEntity<Void> updateUsedMapStatus(@RequestBody Map<String, Object> requestBody) {
        try {
            String lobbyId = (String) requestBody.get("lobbyId");
            Boolean usedCustomMap = (Boolean) requestBody.get("usedCustomMap");

            Lobby lobby = lobbyManagerService.findLobbyByLobbyId(lobbyId);
            if (lobby == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            lobby.setUsedCustomMap(usedCustomMap);
            log.info(lobbyId + " have the staus of used custom map: " + usedCustomMap.toString());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error occurred: ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    /**
     * Retrieve the current playing time for a specific lobby.
     *
     * @param lobbyId the ID of the lobby to fetch the playing time for
     * @return the remaining playing time in milliseconds
     */
    @GetMapping("/lobby/{lobbyId}/current-playing-time")
    public ResponseEntity<Long> getCurrentPlayTime(@PathVariable("lobbyId") String lobbyId) {
        long gameStartTime = lobbyManagerService.findLobbyByLobbyId(lobbyId).getGameStartTime();
        long currentTime = System.currentTimeMillis();

        long currentPlayingTime = GameConfig.PLAYING_TIME - (currentTime - gameStartTime);

        return ResponseEntity.ok(currentPlayingTime);
    }
}
