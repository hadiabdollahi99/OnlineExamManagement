package ir.maktabsharif.onlineexammanagement.controller;

import ir.maktabsharif.onlineexammanagement.model.*;
import ir.maktabsharif.onlineexammanagement.service.CourseService;
import ir.maktabsharif.onlineexammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;

    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) String firstName,
                            @RequestParam(required = false) String lastName,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) String status,
                            Model model) {
        UserRole userRole = null;
        UserStatus userStatus = null;

        if (role != null && !role.isEmpty()) {
            userRole = UserRole.valueOf(role.toUpperCase());
        }
        if (status != null && !status.isEmpty()) {
            userStatus = UserStatus.valueOf(status.toUpperCase());
        }

        List<User> users = userService.searchUsers(firstName, lastName, userRole, userStatus);

        model.addAttribute("users", users);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/users";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long pendingUsers = userService.countByStatus(UserStatus.PENDING);
        long approveUsers = userService.countByStatus(UserStatus.APPROVE);
        long students = userService.countByRole(UserRole.STUDENT);
        long courses = courseService.getTotalCount();
        model.addAttribute("pendingUsers", pendingUsers);
        model.addAttribute("approveUsers", approveUsers);
        model.addAttribute("students", students);
        model.addAttribute("courses", courses);
        return "admin/dashboard";
    }

    @GetMapping("/users/pending")
    public String listPendingUsers(Model model) {
        List<User> pendingUsers = userService.getPendingUsers();
        model.addAttribute("users", pendingUsers);
        return "admin/pending-users";
    }

    @PostMapping("/users/{id}/approve")
    public String approveUser(@PathVariable Long id) {
        userService.approveUser(id);
        return "redirect:/admin/users/pending";
    }

    @PostMapping("/users/{id}/reject")
    public String rejectUser(@PathVariable Long id) {
        userService.rejectUser(id);
        return "redirect:/admin/users/pending";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        return "admin/edit-user";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") User user,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            return "admin/edit-user";
        }

        userService.updateUser(id, user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        User user = userService.getUserById(id).orElseThrow(() -> new RuntimeException("User cannot found!"));
        if (user.getRole().name().equals("TEACHER")){
            List<Course> teacherCourses = courseService.findByTeacher((Teacher) user);
            teacherCourses.forEach(course -> course.setTeacher(null));
        }
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/courses")
    public String listCourses(@RequestParam(required = false) String title,
                              @RequestParam(required = false) String courseCode,
                              Model model) {
        List<Course> courses = courseService.searchCourses(title, courseCode);
        model.addAttribute("courses", courses);
        return "admin/courses";
    }

    @GetMapping("/courses/new")
    public String showCreateCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "admin/create-course";
    }

    @PostMapping("/courses/new")
    public String createCourse(@ModelAttribute("course") Course course,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "admin/create-course";
        }

        courseService.createCourse(course);
        return "redirect:/admin/courses";
    }

    @GetMapping("/courses/{id}")
    public String viewCourse(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        List<User> teachers = userService.getUsersByRole(UserRole.TEACHER);
        List<User> students = userService.getUsersByRole(UserRole.STUDENT);

        model.addAttribute("course", course);
        model.addAttribute("teachers", teachers);
        model.addAttribute("students", students);
        model.addAttribute("courseStudents", courseService.getCourseStudents(id));

        return "admin/course-details";
    }

    @PostMapping("/courses/{courseId}/add-teacher")
    public String addTeacherToCourse(@PathVariable Long courseId,
                                     @RequestParam Long teacherId) {
        courseService.addTeacherToCourse(courseId, teacherId);
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/courses/{courseId}/add-student")
    public String addStudentToCourse(@PathVariable Long courseId,
                                     @RequestParam Long studentId) {
        courseService.addStudentToCourse(courseId, studentId);
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/courses/{courseId}/remove-student/{studentId}")
    public String removeStudentFromCourse(@PathVariable Long courseId,
                                          @PathVariable Long studentId) {
        courseService.removeStudentFromCourse(courseId, studentId);
        return "redirect:/admin/courses/" + courseId;
    }

    @GetMapping("/courses/{id}/edit")
    public String showEditCourseForm(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        model.addAttribute("course", course);
        return "admin/edit-course";
    }

    @PostMapping("/courses/{id}/edit")
    public String updateCourse(@PathVariable Long id,
                               @ModelAttribute("course") Course course,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "admin/edit-course";
        }

        courseService.updateCourse(id, course);
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/admin/courses";
    }
}