package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PlayerService {
    List<Player> getPlayers(
            String name,
            String title,
            Race race,
            Profession profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel
    );

    List<Player> sortedPlayers(List<Player> players, PlayerOrder order);

    List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize);


    Player updatePlayer(Player oldPlayer, Player newPlayer);

    void deletePlayer(Player playerForDelete);

    Player getPlayerById(String id);

    Player savePlayer(Player player);

    Player createNewPlayer(Player player);
}