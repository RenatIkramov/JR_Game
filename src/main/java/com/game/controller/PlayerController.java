package com.game.controller;

import com.game.entity.Player;
import com.game.service.PlayersDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class PlayerController {
    private final PlayersDataService playersDataService;

    @Autowired
    public PlayerController(PlayersDataService playersDataService) {
        this.playersDataService = playersDataService;
    }

    @PostMapping(value = "/rest/players")
    public ResponseEntity<Player> create(@RequestBody Player player) {

        player = playersDataService.create(player);
        return player == null ? new ResponseEntity<>(HttpStatus.BAD_REQUEST)
                : new ResponseEntity<>(player, HttpStatus.OK);
    }



    @GetMapping(value = "/rest/players")
    public ResponseEntity<List<Player>> readAll(@RequestParam Map<String, String> requestParam) {
        List<Player> players = playersDataService.read(requestParam);

        return players == null ? new ResponseEntity<>(HttpStatus.BAD_REQUEST)
                : new ResponseEntity<>(players, HttpStatus.OK);

    }


    @GetMapping(value = "/rest/players/count")
    public ResponseEntity<Integer> count(@RequestParam Map<String, String> requestParam) {
        Integer count = playersDataService.count(requestParam);
        return count == null ? new ResponseEntity<>(HttpStatus.BAD_REQUEST)
                : new ResponseEntity<>(count, HttpStatus.OK);
    }


    @GetMapping(value = "/rest/players/{id}")
    public ResponseEntity<Player> read(@PathVariable(name = "id") Long id) {
        if(id<=0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        final Player Player = playersDataService.read(id);
        return Player != null
                ? new ResponseEntity<>(Player, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @PostMapping(value = "/rest/players/{id}")
    public ResponseEntity<Player> update(@PathVariable(name = "id") Long id, @RequestBody Player player) {
        if(id<=0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Player oldPlayer = playersDataService.read(id);
        if (oldPlayer == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        player.setId(id);
        player = playersDataService.update(oldPlayer, player);
        return player != null
                ? new ResponseEntity<>(player, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @DeleteMapping(value = "/rest/players/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") Long id) {
        if(id<=0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        final boolean deleted = playersDataService.delete(id);

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


}

