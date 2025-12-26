package ir.maktabsharif.onlineexammanagement.service;

import ir.maktabsharif.onlineexammanagement.dto.UserDto;
import ir.maktabsharif.onlineexammanagement.exception.UserExistException;
import ir.maktabsharif.onlineexammanagement.exception.UserNotFoundException;
import ir.maktabsharif.onlineexammanagement.model.*;
import ir.maktabsharif.onlineexammanagement.repository.StudentRepository;
import ir.maktabsharif.onlineexammanagement.repository.TeacherRepository;
import ir.maktabsharif.onlineexammanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(UserDto dto, UserRole userRole) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UserExistException("نام کاربری قبلاً ثبت شده است");
        }

        int randomNum = ThreadLocalRandom.current().nextInt(1000, 100000);
        String randomString = String.valueOf(randomNum);


        if (userRole.name().equals("STUDENT")){
            Student student = Student.builder()
                    .username(dto.getUsername())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .email(dto.getEmail())
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .role(UserRole.STUDENT)
                    .status(UserStatus.PENDING)
                    .studentNumber(randomString)
                    .build();

            return studentRepository.save(student);

        } else {
            Teacher teacher = Teacher.builder()
                    .username(dto.getUsername())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .email(dto.getEmail())
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .role(UserRole.TEACHER)
                    .status(UserStatus.PENDING)
                    .teacherNumber(randomString)
                    .build();

            return teacherRepository.save(teacher);
        }

    }

    @Transactional
    public User approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("کاربر یافت نشد"));
        user.setStatus(UserStatus.APPROVE);
        return userRepository.save(user);
    }

    @Transactional
    public User rejectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("کاربر یافت نشد"));
        user.setStatus(UserStatus.REJECT);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("کاربر یافت نشد"));

        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> getPendingUsers() {
        return userRepository.findByStatus(UserStatus.PENDING);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> searchUsers(String firstName, String lastName, UserRole role, UserStatus status) {
        return userRepository.searchUsers(firstName, lastName, role, status);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public long countByStatus(UserStatus status){
        return userRepository.countByStatus(status);
    }

    public long countByRole(UserRole role){
        return userRepository.countByRole(role);
    }
}