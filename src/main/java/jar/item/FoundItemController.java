package jar.item;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jar.model.User;
import jar.repo.UserRepository;

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
    public ResponseEntity<?> addFoundItem(
            @RequestBody FoundItem foundItem,
            @RequestParam Long userId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User with ID " + userId + " not found."));
        }

        foundItem.setUser(user);
        FoundItem saved = foundItemRepository.save(foundItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<FoundItem> getFoundItems() {
        return foundItemRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFoundItemById(@PathVariable Long id) {
        return foundItemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((FoundItem) null));
    }
}