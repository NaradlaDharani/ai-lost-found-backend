package jar.item;

import jar.model.User;
import jar.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/found-items")
public class FoundItemController {

    private final FoundItemRepository foundItemRepository;
    private final UserRepository userRepository;

    public FoundItemController(
            FoundItemRepository foundItemRepository,
            UserRepository userRepository) {

        this.foundItemRepository = foundItemRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public FoundItem addFoundItem(
            @RequestBody FoundItem foundItem,
            @RequestParam Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        foundItem.setUser(user);

        return foundItemRepository.save(foundItem);
    }

    @GetMapping
    public List<FoundItem> getFoundItems() {
        return foundItemRepository.findAll();
    }
}