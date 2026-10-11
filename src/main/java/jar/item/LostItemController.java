package jar.item;

import jar.model.User;
import jar.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
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
    public LostItem addLostItem(
            @RequestBody LostItem lostItem,
            @RequestParam Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        lostItem.setUser(user);

        return lostItemRepository.save(lostItem);
    }

    @GetMapping
    public List<LostItem> getLostItems() {
        return lostItemRepository.findAll();
    }
}