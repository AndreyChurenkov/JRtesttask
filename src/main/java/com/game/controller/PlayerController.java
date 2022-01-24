package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/*

 Аннотация @Controller указывает, что данный класс играет роль контроллера,
 потому нет необходимости наследования какого-либо базового класса контроллера или использования Servlet API.

 Основная цель аннотации @Controller - назначать шаблон данному классу, показывая его роль.
 Это значит, что диспетчер будет сканировать Controller-классы на предмет реализованных методов,
 проверяя @RequestMapping аннотации.
 */

@RestController
public class PlayerController {

    private PlayerService playerService;

    /*
     * Свойства класса с аннотацией @Autowired заполняются соответствующими значениями сразу
     * после создания bean'а и перед тем, как любой из методов класса будет вызван.
     */

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /*
     Аннотация для сопоставления HTTP-запросов GET с определенными методами обработчика.
     В частности, @GetMapping — это составленная аннотация, которая действует как ярлык
     для @RequestMapping (метод = RequestMethod.GET).
     */

    //Получение списка всех зарегестрированных игроков
    @GetMapping(path = "/rest/players")
    public List<Player> getAllPlayers (
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(defaultValue = "ID", value = "order") PlayerOrder order,
            @RequestParam(defaultValue = "0", value = "pageNumber") Integer pageNumber,
            @RequestParam(defaultValue = "3", value = "pageSize") Integer pageSize
    ) {
        final List<Player> players = playerService.getPlayers(name, title, race, profession, after, before, banned,
                minExperience, maxExperience, minLevel, maxLevel);

        final List<Player> sortedPlayers = playerService.sortedPlayers(players, order);

      return playerService.getPage(sortedPlayers, pageNumber, pageSize);
    }

    //Создание нового игрока, в случае несовпадения критериев необходимо ответить ошибкой с кодом 400
    @PostMapping(path = "/rest/players")
    public Player createPlayer(@RequestBody Player player) {
        return playerService.createNewPlayer(player);
    }


    //Получение количества игроков, которые соответствуюь фильтрам
    @GetMapping(path = "/rest/players/count")
    public Integer getPlayersCount (
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel
    ) {

        List<Player> playerList = playerService.getPlayers(name, title, race, profession, after,
                before, banned, minExperience, maxExperience, minLevel, maxLevel);

        return playerList.size();
    }

    //Метод для получения игрока по его id. Изменил вчера вечером, подправить недочеты и сделать красиво
    @GetMapping(path = "/rest/players/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable String id) {
        return new ResponseEntity<>(playerService.getPlayerById(id), HttpStatus.OK);
    }

    @PostMapping(path = "/rest/players/{id}")
    @ResponseBody
    public Player updatePlayer(@PathVariable String id, @RequestBody Player player) {
        final Player savedPlayer = playerService.getPlayerById(id);
        return playerService.updatePlayer(savedPlayer, player);
    }

    //Метод для удаления игрока. Изменил вчера вечером, подправить недочеты
    @DeleteMapping(path = "/rest/players/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable String id) {
        playerService.deletePlayer(playerService.getPlayerById(id));
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
