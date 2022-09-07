package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlayersDataService {


    private final PlayerJpaRepository repository;

    @Autowired
    public PlayersDataService(PlayerJpaRepository repository) {
        this.repository = repository;
    }

    private boolean canCreate(Player player) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

        final int NAME_LENGTH_LIMIT = 12;
        final int TITLE_LENGTH_LIMIT = 30;
        final int EXPERIENCE_MIN_VALUE = 0;
        final int EXPERIENCE_MAX_VALUE = 10_000_000;
        final Date MIN_DATE;
        final Date MAX_DATE;
        try {
            MIN_DATE = simpleDateFormat.parse("01.01.2000");
            MAX_DATE = simpleDateFormat.parse("01.01.3000");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (player.getName() == null
                || player.getName().length() > NAME_LENGTH_LIMIT
                || player.getName().isEmpty()
                || player.getTitle() == null
                || player.getTitle().length() > TITLE_LENGTH_LIMIT
                || player.getRace() == null
                || player.getProfession() == null
                || player.getBirthday() == null
                || player.getBirthday().before(MIN_DATE)
                || player.getBirthday().after(MAX_DATE)
                || player.getExperience() == null
                || player.getExperience() < EXPERIENCE_MIN_VALUE
                || player.getExperience() > EXPERIENCE_MAX_VALUE
        ) {
            return false;
        }
        return true;
    }


    public Player create(Player player) {
        if (!canCreate(player))
            return null;
        if (player.getBanned() == null)
            player.setBanned(false);
        calculateLevel(player);
        calculateUntilNextLevel(player);
        repository.save(player);
        return player;
    }

    public List<Player> read(Map<String, String> requestParam) {
        PlayerOrder order = PlayerOrder.ID;
        int pageNumber = 0;
        int pageSize = 3;
        if (requestParam.containsKey("order"))
            order = PlayerOrder.valueOf(requestParam.get("order"));
        List<Player> players = repository.findAll(Sort.by(order.getFieldName()));

        players = setFilters(requestParam, players);
        if (players == null)
            return null;

        if (requestParam.containsKey("pageNumber"))
            pageNumber = Integer.parseInt(requestParam.get("pageNumber"));
        if (requestParam.containsKey("pageSize"))
            pageSize = Integer.parseInt(requestParam.get("pageSize"));


        players = players.stream()
                .skip((long) pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        return players;
    }

    public Integer count(Map<String, String> requestParam) {
        if (requestParam.isEmpty())
            return (int) repository.count();
        List<Player> players = repository.findAll();
        players = setFilters(requestParam, players);


        return players == null ? null : players.size();
    }

    public Player read(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Player update(Player oldPlayer, Player newPlayer) {

        if (newPlayer.getName() == null)
            newPlayer.setName(oldPlayer.getName());
        if (newPlayer.getTitle() == null)
            newPlayer.setTitle(oldPlayer.getTitle());
        if (newPlayer.getRace() == null)
            newPlayer.setRace(oldPlayer.getRace());
        if (newPlayer.getProfession() == null)
            newPlayer.setProfession(oldPlayer.getProfession());
        if (newPlayer.getBirthday() == null)
            newPlayer.setBirthday(oldPlayer.getBirthday());
        if (newPlayer.getBanned() == null)
            newPlayer.setBanned(oldPlayer.getBanned());
        if (newPlayer.getExperience() == null)
            newPlayer.setExperience(oldPlayer.getExperience());

        if (!canCreate(newPlayer))
            return null;
        calculateLevel(newPlayer);
        calculateUntilNextLevel(newPlayer);

        return repository.save(newPlayer);
    }


    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        } else
            return false;
    }

    private static List<Player> setFilters(Map<String, String> requestParam, List<Player> players) {
        try {
            if (requestParam.containsKey("name")) {
                String name = requestParam.get("name").toLowerCase();
                players = players.stream()
                        .filter(player -> player.getName().toLowerCase().contains(name))
                        .collect(Collectors.toList());
            }
            if (requestParam.containsKey("title")) {
                String title = requestParam.get("title").toLowerCase();
                players = players.stream()
                        .filter(player -> player.getTitle().toLowerCase().contains(title))
                        .collect(Collectors.toList());
            }
            if (requestParam.containsKey("race")) {
                Race race = Race.valueOf(requestParam.get("race").toUpperCase());
                players = players.stream()
                        .filter(player -> player.getRace() == race)
                        .collect(Collectors.toList());
            }
            if (requestParam.containsKey("profession")) {
                Profession profession = Profession.valueOf(requestParam.get("profession").toUpperCase());
                players = players.stream()
                        .filter(player -> player.getProfession() == profession)
                        .collect(Collectors.toList());
            }
            if (requestParam.containsKey("after")) {
                Date after = new Date(Long.parseLong(requestParam.get("after")));
                players = players.stream()
                        .filter(player -> player.getBirthday().after(after))
                        .collect(Collectors.toList());
            }
            if (requestParam.containsKey("before")) {
                Date before = new Date(Long.parseLong(requestParam.get("before")));
                players = players.stream()
                        .filter(player -> player.getBirthday().before(before))
                        .collect(Collectors.toList());
            }
            if (requestParam.containsKey("minExperience")) {
                int minExperience = Integer.parseInt(requestParam.get("minExperience"));
                players = players.stream()
                        .filter(player -> minExperience <= player.getExperience())
                        .collect(Collectors.toList());
            }
            if (requestParam.containsKey("maxExperience")) {
                int maxExperience = Integer.parseInt(requestParam.get("maxExperience"));
                players = players.stream()
                        .filter(player -> player.getExperience() <= maxExperience)
                        .collect(Collectors.toList());
            }
            if (requestParam.containsKey("minLevel")) {
                int minLevel = Integer.parseInt(requestParam.get("minLevel"));
                players = players.stream()
                        .filter(player -> minLevel <= player.getLevel())
                        .collect(Collectors.toList());
            }
            if (requestParam.containsKey("maxLevel")) {
                int maxLevel = Integer.parseInt(requestParam.get("maxLevel"));
                players = players.stream()
                        .filter(player -> player.getLevel() <= maxLevel)
                        .collect(Collectors.toList());
            }
            if (requestParam.containsKey("banned")) {
                boolean banned = Boolean.parseBoolean(requestParam.get("banned").toLowerCase());
                players = players.stream()
                        .filter(player -> player.getBanned() == banned)
                        .collect(Collectors.toList());

            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        return players;
    }

    public void calculateLevel(Player player) {
        int level = (int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100);
        player.setLevel(level);
    }

    public void calculateUntilNextLevel(Player player) {
        int level = player.getLevel();
        int experience = player.getExperience();
        int untilNextLevel = (50 * (level + 1) * (level + 2)) - experience;
        player.setUntilNextLevel(untilNextLevel);
    }

}
