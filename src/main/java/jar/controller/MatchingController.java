package jar.controller;

import org.springframework.web.bind.annotation.*;
import jar.service.MatchingService;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
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