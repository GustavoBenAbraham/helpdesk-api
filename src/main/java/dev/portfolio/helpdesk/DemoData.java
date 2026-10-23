package dev.portfolio.helpdesk;
import org.springframework.context.annotation.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
@Profile("demo")
public class DemoData {
    @Bean CommandLineRunner demo(AccountRepository accounts, PasswordEncoder encoder) {
        return args -> {
            if(accounts.findByEmail("agente@demo.local").isEmpty()) accounts.save(new Account("agente@demo.local",encoder.encode("DemoSenha123!"),"Agente Demo",Account.Role.AGENT));
        };
    }
}
