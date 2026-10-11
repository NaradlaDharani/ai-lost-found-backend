package jar.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jar.service.MatchingService;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "http://localhost:5176")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/{lostItemId}")
    public List<MatchingService.MatchResult> findMatches(
            @PathVariable Long lostItemId) {

        return matchingService.findMatches(lostItemId);
    }
}