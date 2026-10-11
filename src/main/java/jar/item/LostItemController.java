
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
@RequestMapping("/api/lost-items")
@CrossOrigin(origins = "http://localhost:5176")
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