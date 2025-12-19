package ir.maktabsharif.onlineexammanagement.bootStrap;

import ir.maktabsharif.onlineexammanagement.model.User;
import ir.maktabsharif.onlineexammanagement.model.UserRole;
import ir.maktabsharif.onlineexammanagement.model.UserStatus;
import ir.maktabsharif.onlineexammanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .firstName("admin")
                    .lastName("adminian")
                    .email("admin@email.com")
                    .status(UserStatus.APPROVE)
                    .role(UserRole.ADMIN)
                    .build();

            userRepository.save(admin);
        }

    }
}