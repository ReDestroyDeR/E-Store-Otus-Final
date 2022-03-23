package ru.red.authenticationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.red.authenticationservice.dto.UserDetachedDTO;
import ru.red.authenticationservice.exception.BadEmailException;
import ru.red.authenticationservice.exception.BadPasswordException;
import ru.red.authenticationservice.exception.BadRequestException;
import ru.red.authenticationservice.service.UserService;

import static ru.red.authenticationservice.util.EmailUtil.isEmail;

/**
 * Controller responsible for handling user registration, patching, signing-in <i>Handing out JWTs</i> and removal.
 * All endpoints have no authority checks so 403 must be impossible
 */
@RestController
@RequestMapping("/")
public class AuthController {

    private final UserService userService;

    /**
     * @param userService service implementation used as backend for all operations
     */
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint serving JWTs
     *
     * @param userDetachedDTO user credentials containing user and password pair
     * @return 200 {@link String} JSON Web Token that will be used in higher-level authentication<br>
     * 403 {@link BadPasswordException} If credentials are invalid
     */
    @PostMapping("login")
    public Mono<String> login(@RequestBody UserDetachedDTO userDetachedDTO) {
        return userService.login(userDetachedDTO);
    }

    /**
     * Registration endpoint
     *
     * @param userDetachedDTO user credentials
     * @return 200 {@link Void} User has been created<br>
     * 400 {@link BadRequestException} Email is already occupied
     */
    @PostMapping("register")
    public Mono<Void> register(@RequestBody UserDetachedDTO userDetachedDTO) {
        if (!isEmail(userDetachedDTO.getEmail())) {
            return Mono.error(new BadEmailException());
        }

        return userService.registerUser(userDetachedDTO).then();
    }

    @PostMapping("change-email")
    public Mono<Void> changeEmail(@RequestParam("email") String email,
                                  @RequestBody UserDetachedDTO userDetachedDTO) {
        if (!isEmail(email)) {
            return Mono.error(new BadEmailException());
        }

        return userService.updateEmail(email, userDetachedDTO).then();
    }

    @PostMapping("change-password")
    public Mono<Void> changePassword(@RequestParam("password") String password,
                                     @RequestBody UserDetachedDTO userDetachedDTO) {
        return userService.updatePassword(password, userDetachedDTO).then();
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@RequestBody UserDetachedDTO userDetachedDTO) {
        return userService.delete(userDetachedDTO);
    }
}
