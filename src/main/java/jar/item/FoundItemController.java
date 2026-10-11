
package jar.item;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jar.model.User;
import jar.repo.UserRepository;

@RestController
@RequestMapping("/api/found-items")
@CrossOrigin(origins = "http://localhost:5176")
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