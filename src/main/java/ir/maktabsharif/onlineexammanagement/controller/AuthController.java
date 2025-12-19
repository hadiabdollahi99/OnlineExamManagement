package ir.maktabsharif.onlineexammanagement.controller;

import ir.maktabsharif.onlineexammanagement.model.User;
import ir.maktabsharif.onlineexammanagement.model.UserRole;
import ir.maktabsharif.onlineexammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {


    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "نام کاربری یا رمز عبور نادرست است");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "شما با موفقیت خارج شدید");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }


    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               BindingResult result,
                               @RequestParam("role") String role,
                               Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        if (userService.getUserByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("errorMessage", "این نام کاربری قبلاً ثبت شده است");
            return "register";
        }

        if (userService.getUserByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("errorMessage", "این ایمیل قبلاً ثبت شده است");
            return "register";
        }

        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            User registeredUser = userService.registerUser(user, userRole);

            model.addAttribute("username", registeredUser.getUsername());
            model.addAttribute("role", registeredUser.getRole().name());

            return "registration-success";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "خطا در ثبت نام: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String username = auth.getName();
            var authorities = auth.getAuthorities();

            model.addAttribute("username", username);
            model.addAttribute("authorities", authorities);

            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"))) {
                return "redirect:/teacher/dashboard";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
                return "redirect:/student/dashboard";
            }
        }

        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}