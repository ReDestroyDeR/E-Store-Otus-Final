package ru.red.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.red.domain.UserBilling;
import ru.red.dto.UserIdentityDTO;
import ru.red.service.UserBillingService;

@RestController
@RequestMapping("/api/billing")
public class UserBillingController {
    private final UserBillingService service;

    @Autowired
    public UserBillingController(UserBillingService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<UserBilling> create(@RequestBody UserIdentityDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/id/{id}")
    public Mono<UserBilling> findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/email/{email}")
    public Mono<UserBilling> findByEmail(@PathVariable String email) {
        return service.findByEmail(email);
    }

    @PostMapping("/funds/add")
    public Mono<UserBilling> addFundsToUser(@RequestParam String email,
                                            @RequestParam int add) {
        return service.addFundsToUser(email, add);
    }

    @PostMapping("/funds/sub")
    public Mono<UserBilling> removeFundsFromUser(@RequestParam String email,
                                                 @RequestParam int sub) {
        return service.removeFundsFromUser(email, sub);
    }
}
