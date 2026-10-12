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
@RequestMapping("/api/lost-items")
public class LostItemController {

    private final LostItemRepository lostItemRepository;
    private final UserRepository userRepository;

    public LostItemController(
            LostItemRepository lostItemRepository,
            UserRepository userRepository) {

        this.lostItemRepository = lostItemRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> addLostItem(
            @RequestBody LostItem lostItem,
            @RequestParam Long userId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User with ID " + userId + " not found."));
        }

        lostItem.setUser(user);
        LostItem saved = lostItemRepository.save(lostItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<LostItem> getLostItems() {
        return lostItemRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLostItemById(@PathVariable Long id) {
        return lostItemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((LostItem) null));
    }
}