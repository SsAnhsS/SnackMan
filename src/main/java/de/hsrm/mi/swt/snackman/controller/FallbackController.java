package de.hsrm.mi.swt.snackman.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FallbackController {

    @RequestMapping(value = "/LobbyView/**")
    public String redirectLobbyView() {
        return "forward:/index.html";
    }

    @RequestMapping(value = "/LobbyListView/**")
    public String redirectLobbyListView() {
        return "forward:/index.html";
    }

    @RequestMapping(value = "/Leaderboard/**")
    public String redirectLeaderboard() {
        return "forward:/index.html";
    }
}
