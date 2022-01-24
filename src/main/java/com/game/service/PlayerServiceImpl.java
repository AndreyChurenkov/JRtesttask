package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.*;

/*Аннотация @Service (Слой-сервис приложения) объявляет,
        что данный класс представляет собой сервис - компонент сервис-слоя,
        который содержит определенную логику*/

/* Аннотация @Repository (доменный слой) показывает, что класс функционирует как репозиторий и требует наличия прозрачной
 * трансляции исключений. Преимуществом трансляции исключений является то, что слой сервиса будет
 * иметь дело с общей иерархией исключений от Spring (DataAccessException) вне зависимости
 * от используемых технологий доступа к данным в слое данных.
 */
/* Перед исполнением метода помеченного аннотацией @Transactional начинается транзакция,
 * после выполнения метода транзакция коммитится, при выбрасывании RuntimeException откатывается.
 * @return list of books
 */
@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }


    @Override
    public List<Player> getPlayers(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        final List<Player> list = new ArrayList<>();

        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);


        playerRepository.findAll().forEach(player -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (after != null && player.getBirthday().before(afterDate)) return;
            if (before != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned() != banned) return;
            if (minExperience != null && player.getExperience() < minExperience) return;
            if (maxExperience != null && player.getExperience() > maxExperience) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;
            list.add(player);
        });
        return list;
    }

    @Override
    public List<Player> sortedPlayers(List<Player> players, PlayerOrder order) {
        if (order != null) {
            players.sort((player1, player2) -> {
                switch (order) {
                    case NAME:
                        return player1.getName().compareTo(player2.getName());
                    case EXPERIENCE:
                        return player1.getExperience().compareTo(player2.getExperience());
                    case BIRTHDAY:
                        return player1.getBirthday().compareTo(player2.getBirthday());
                    default:
                        return player1.getId().compareTo(player2.getId());
                }
            });
        }
        return players;
    }

    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 1 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return players.subList(from, to);
    }

    @Override
    public Player updatePlayer(Player oldPlayer, Player newPlayer) {

        if (NullArguments(newPlayer)) {
            return oldPlayer;
        }

        if (isNameValid(newPlayer)) {
            oldPlayer.setName(newPlayer.getName());
        }

        if (isTitleValid(newPlayer)) {
            oldPlayer.setTitle(newPlayer.getTitle());
        }

        if (newPlayer.getRace() != null) {
            oldPlayer.setRace(newPlayer.getRace());
        }

        if (newPlayer.getProfession() != null) {
            oldPlayer.setProfession(newPlayer.getProfession());
        }

        if (newPlayer.getExperience() != null) {
            if (isExperienceValid(newPlayer)) {
                oldPlayer.setExperience(newPlayer.getExperience());
            } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (newPlayer.getBirthday() != null) {
            if (isBirthdayValid(newPlayer)) {
                oldPlayer.setBirthday(newPlayer.getBirthday());
            } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (newPlayer.getBanned() != null) {
            oldPlayer.setBanned(newPlayer.getBanned());
        }

        oldPlayer.setLevel((int) (Math.sqrt(2500 + 200 * oldPlayer.getExperience()) - 50) / 100);

        oldPlayer.setUntilNextLevel(50 * (oldPlayer.getLevel() + 1) * (oldPlayer.getLevel() + 2) - oldPlayer.getExperience());

        return savePlayer(oldPlayer);
    }

    @Override
    public void deletePlayer(Player playerForDelete) {
        playerRepository.delete(playerForDelete);
    }

    @Override
    public Player getPlayerById(String id) {
        Long newId;
        try {
            newId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(400));
        }
        if (!(newId > 0)) throw new ResponseStatusException(HttpStatus.valueOf(400));

        if (playerRepository.existsById(newId)) {
            return playerRepository.findById(newId).get();
        } else {
            throw new ResponseStatusException(HttpStatus.valueOf(404));
        }
    }

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public Player createNewPlayer(Player player) {
        if (!isValidPlayer(player)) {
            throw new ResponseStatusException(HttpStatus.valueOf(400));
        } else {
            player.setId(playerRepository.count() + 1);
            //Здесь формула для уровня
            player.setLevel((int) (Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100);

            //Здесь формула для опыта до следующего уровня
            player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());

            return savePlayer(player);
        }
    }

    public boolean NullArguments(Player player) {
        return player.getId() == null && player.getName() == null && player.getTitle() == null && player.getBirthday() == null && player.getRace() == null && player.getProfession() == null && player.getExperience() == null;
    }

    //Возможно стоит добавть валидный день рождения
    public boolean isValidPlayer(Player player) {
        return isNameValid(player) && isTitleValid(player) && isRaceValid(player) && isProfessionValid(player) && isExperienceValid(player) && isBirthdayValid(player);
    }

    public boolean isNameValid(Player player) {
        boolean test = player.getName() != null && !player.getName().isEmpty() && player.getName().length() <= 12;
        return test;
    }

    public boolean isTitleValid(Player player) {
        boolean test = player.getTitle() != null && !player.getTitle().isEmpty() && player.getTitle().length() <= 30;
        return test;
    }

    public boolean isRaceValid(Player player) {
        boolean test = player.getRace() != null;
        return test;
    }

    public boolean isProfessionValid(Player player) {
        boolean test = player.getProfession() != null;
        return test;
    }

    public boolean isExperienceValid(Player player) {
        boolean test = player.getExperience() != null && player.getExperience() >= 0 && player.getExperience() <= 10_000_000;
        return test;
    }

    public boolean isBirthdayValid(Player player) {
        boolean test = player.getBirthday() != null && player.getBirthday().after(new GregorianCalendar(1999, Calendar.DECEMBER, 31).getTime())
                && player.getBirthday().before(new GregorianCalendar(3000, Calendar.DECEMBER, 31).getTime());
        return test;
    }

}
