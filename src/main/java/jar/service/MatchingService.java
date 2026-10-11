package jar.service;

import jar.item.FoundItem;
import jar.item.FoundItemRepository;
import jar.item.LostItem;
import jar.item.LostItemRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MatchingService {

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;

    public MatchingService(
            LostItemRepository lostItemRepository,
            FoundItemRepository foundItemRepository) {

        this.lostItemRepository = lostItemRepository;
        this.foundItemRepository = foundItemRepository;
    }

    public List<MatchResult> findMatches(Long lostItemId) {

        LostItem lostItem = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new RuntimeException("Lost item not found"));

        List<FoundItem> foundItems = foundItemRepository.findAll();
        List<MatchResult> matches = new ArrayList<>();

        for (FoundItem foundItem : foundItems) {

            int score = 0;

            // Item name - 30 points
            if (sameText(lostItem.getItemName(), foundItem.getItemName())) {
                score += 30;
            }

            // Category - 20 points
            if (sameText(lostItem.getCategory(), foundItem.getCategory())) {
                score += 20;
            }

            // Location - 20 points
            if (sameText(lostItem.getLocation(), foundItem.getLocation())) {
                score += 20;
            }

            // Description - 20 points
            score += getDescriptionScore(
                    lostItem.getDescription(),
                    foundItem.getDescription());

            // Date - 10 points
            score += getDateScore(
                    lostItem.getLostDate(),
                    foundItem.getFoundDate());

            // Only show possible matches
            if (score >= 50) {
                matches.add(new MatchResult(foundItem, score));
            }
        }

        // Highest score first
        matches.sort(
                Comparator.comparingInt(MatchResult::getMatchScore)
                        .reversed());

        return matches;
    }

    private boolean sameText(String first, String second) {

        if (first == null || second == null) {
            return false;
        }

        return first.trim().equalsIgnoreCase(second.trim());
    }

    private int getDescriptionScore(String lostDescription,
            String foundDescription) {

        if (lostDescription == null || foundDescription == null) {
            return 0;
        }

        String lost = cleanText(lostDescription);
        String found = cleanText(foundDescription);

        if (lost.isEmpty() || found.isEmpty()) {
            return 0;
        }

        // Exact description
        if (lost.equals(found)) {
            return 20;
        }

        Set<String> lostWords = new HashSet<>();

        for (String word : lost.split("\\s+")) {
            if (word.length() > 2) {
                lostWords.add(word);
            }
        }

        Set<String> foundWords = new HashSet<>();

        for (String word : found.split("\\s+")) {
            if (word.length() > 2) {
                foundWords.add(word);
            }
        }

        if (lostWords.isEmpty() || foundWords.isEmpty()) {
            return 0;
        }

        Set<String> commonWords = new HashSet<>(lostWords);
        commonWords.retainAll(foundWords);

        double similarity = (2.0 * commonWords.size())
                / (lostWords.size() + foundWords.size());

        if (similarity >= 0.80) {
            return 20;
        } else if (similarity >= 0.60) {
            return 16;
        } else if (similarity >= 0.40) {
            return 12;
        } else if (similarity >= 0.20) {
            return 7;
        } else if (commonWords.size() == 1) {
            return 4;
        }

        return 0;
    }

    private String cleanText(String text) {

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int getDateScore(String lostDate,
            String foundDate) {

        if (lostDate == null || foundDate == null) {
            return 0;
        }

        try {

            LocalDate lost = LocalDate.parse(lostDate);
            LocalDate found = LocalDate.parse(foundDate);

            long difference = Math.abs(ChronoUnit.DAYS.between(lost, found));

            if (difference == 0) {
                return 10;
            } else if (difference <= 1) {
                return 8;
            } else if (difference <= 3) {
                return 5;
            } else if (difference <= 7) {
                return 2;
            }

        } catch (Exception ignored) {
            // Invalid date
        }

        return 0;
    }

    public static class MatchResult {

        private Long id;
        private String itemName;
        private String description;
        private String category;
        private String location;
        private String foundDate;
        private int matchScore;

        public MatchResult(FoundItem foundItem, int matchScore) {

            this.id = foundItem.getId();
            this.itemName = foundItem.getItemName();
            this.description = foundItem.getDescription();
            this.category = foundItem.getCategory();
            this.location = foundItem.getLocation();
            this.foundDate = foundItem.getFoundDate();
            this.matchScore = matchScore;
        }

        public Long getId() {
            return id;
        }

        public String getItemName() {
            return itemName;
        }

        public String getDescription() {
            return description;
        }

        public String getCategory() {
            return category;
        }

        public String getLocation() {
            return location;
        }

        public String getFoundDate() {
            return foundDate;
        }

        public int getMatchScore() {
            return matchScore;
        }
    }
}