package dev.portfolio.helpdesk;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import static dev.portfolio.helpdesk.ApiModels.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountRepository accounts;
    private final PasswordEncoder passwords;
    public AuthController(AccountRepository accounts, PasswordEncoder passwords) { this.accounts=accounts; this.passwords=passwords; }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountView register(@Valid @RequestBody Registration input) {
        String email = input.email().toLowerCase(java.util.Locale.ROOT);
        if (accounts.findByEmail(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"E-mail já cadastrado");
        // BCrypt accepts at most 72 bytes, which differs from character count for Unicode.
        if(input.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Senha deve ter no máximo 72 bytes UTF-8");
        return AccountView.of(accounts.save(new Account(email,passwords.encode(input.password()),input.name(),Account.Role.CUSTOMER)));
    }
    @GetMapping("/me") public AccountView me(Authentication auth) { return AccountView.of(accounts.findByEmail(auth.getName()).orElseThrow()); }
}
