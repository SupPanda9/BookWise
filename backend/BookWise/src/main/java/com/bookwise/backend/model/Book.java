package com.bookwise.backend.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Book {
    private String googleBooksId; // ID от Google Books API
    private String title; // Заглавие на книгата
    private List<String> authors; // Списък с автори
    private List<String> genres; // Списък с жанрове
    private String description; // Описание на книгата
    private Integer pageCount; // Брой страници
    private String coverImage; // Линк към корицата
    private String isbn; // ISBN код
    private Map<String, Popularity> popularity; // Данни за популярността

    private static final int LAST_7_DAYS = 7;
    private static final int LAST_30_DAYS = 30;

    @Data
    public static class Popularity {
        private List<Integer> days; // За последните 7 или 30 дни
        private Integer total; // Обща популярност за периода

        public Popularity() { }

        public Popularity(int daysCount) {
            this.days = new ArrayList<>(Collections.nCopies(daysCount, 0));
            this.total = 0;
        }
    }

    // Метод за увеличаване на популярността за текущия ден
    public void incrementPopularity() {
        if (popularity == null) {
            initializePopularity();
        }

        // Увеличи популярността за последните 7 дни
        incrementPeriod("last7Days", LAST_7_DAYS);
        // Увеличи популярността за последните 30 дни
        incrementPeriod("last30Days", LAST_30_DAYS);

        // Увеличи популярността за текущата година
        popularity.computeIfAbsent("thisYear", k -> new Popularity(0)).setTotal(
            popularity.get("thisYear").getTotal() + 1
        );
    }

    private void incrementPeriod(String key, int maxDays) {
        Popularity period = popularity.computeIfAbsent(key, k -> new Popularity(maxDays));
        List<Integer> days = period.getDays();

        // Изчисли текущия индекс за деня
        int todayIndex = LocalDate.now().getDayOfMonth() % maxDays;
        days.set(todayIndex, days.get(todayIndex) + 1);

        // Обнови общия брой за периода
        period.setTotal(days.stream().mapToInt(Integer::intValue).sum());
    }

    private void initializePopularity() {
        popularity = new HashMap<>();
        popularity.put("last7Days", new Popularity(LAST_7_DAYS)); // Само 7 дни
        popularity.put("last30Days", new Popularity(LAST_30_DAYS)); // Само 30 дни

        // Инициализиране на thisYear без дни
        Popularity yearPopularity = new Popularity(0);
        yearPopularity.setTotal(0);
        yearPopularity.setDays(null); // Годишната популярност няма дни
        popularity.put("thisYear", yearPopularity);
    }

}
