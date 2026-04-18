package com.art.tutordesk.lesson;

import com.art.tutordesk.lesson.dto.LessonListDTO;
import com.art.tutordesk.lesson.dto.LessonProfileDTO;
import com.art.tutordesk.lesson.dto.LessonStudentDto;
import com.art.tutordesk.lesson.dto.LessonUpdateForm;
import com.art.tutordesk.lesson.service.LessonService;
import com.art.tutordesk.student.service.StudentService;
import com.art.tutordesk.payment.Currency;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonViewController {

    private final LessonService lessonService;
    private final StudentService studentService;

    @GetMapping("/list")
    public String listLessons(@RequestParam(value = "startDate", required = false) LocalDate startDate,
                              @RequestParam(value = "endDate", required = false) LocalDate endDate,
                              Model model) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = (endDate != null) ? endDate : LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        List<LessonListDTO> lessons = lessonService.getLessonsByDateRange(start, end);
        model.addAttribute("lessons", lessons);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "lesson/list-lessons";
    }

    @GetMapping("/new")
    public String newLessonForm(Model model) {
        model.addAttribute("lesson", new Lesson());
        model.addAttribute("allStudents", studentService.getAllActiveStudents());
        model.addAttribute("currencySymbols", getCurrencySymbolsMap());
        return "lesson/add-lesson";
    }

    @PostMapping("/create")
    public String createLesson(@Valid @ModelAttribute Lesson lesson,
                               BindingResult bindingResult,
                               @RequestParam(value = "selectedStudentIds", required = false) List<Long> selectedStudentIds,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (CollectionUtils.isEmpty(selectedStudentIds)) {
            bindingResult.reject("lesson.students.empty", "At least one student must be selected for the lesson.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allStudents", studentService.getAllActiveStudents()); // Re-add students for form display
            model.addAttribute("currencySymbols", getCurrencySymbolsMap());
            return "lesson/add-lesson";
        }
        lessonService.saveLesson(lesson, selectedStudentIds);
        redirectAttributes.addFlashAttribute("message", "Lesson created successfully!");
        return "redirect:/lessons/list";
    }


    @GetMapping("/profile/{id}")
    public String showLessonProfile(@PathVariable Long id, Model model) {
        LessonProfileDTO lesson = lessonService.getLessonById(id);
        model.addAttribute("lesson", lesson);

        // Calculate if the lesson can be deleted (all students must be SCHEDULED)
        boolean canDeleteLesson = lesson.getStudentAssociations().stream()
                                      .allMatch(ls -> ls.getStatus() == LessonStudentStatus.SCHEDULED);
        model.addAttribute("canDeleteLesson", canDeleteLesson);

        return "lesson/lesson-profile";
    }

    @GetMapping("/edit/{id}")
    public String editLessonForm(@PathVariable Long id, Model model) {
        LessonProfileDTO lesson = lessonService.getLessonById(id);
        model.addAttribute("lesson", lesson); // Contains studentAssociations for initial table render
        model.addAttribute("allStudents", studentService.getAllActiveStudents());
        model.addAttribute("currencySymbols", getCurrencySymbolsMap());

        // Get IDs of students already associated with this lesson for multi-select pre-selection
        List<Long> selectedStudentIds = lesson.getStudentAssociations().stream()
                .map(LessonStudentDto::getStudentId)
                .collect(Collectors.toList());
        model.addAttribute("selectedStudentIds", selectedStudentIds);

        return "lesson/edit-lesson";
    }

    @PostMapping("/update/{id}")
    public String updateLesson(@PathVariable Long id,
                               @ModelAttribute LessonUpdateForm lessonUpdateForm,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (lessonUpdateForm.getStudentUpdates() == null || lessonUpdateForm.getStudentUpdates().isEmpty()) {
            bindingResult.reject("lesson.students.empty", "At least one student must be selected for the lesson.");
        }

        if (bindingResult.hasErrors()) {
            // Repopulate model for the edit form if there are errors
            LessonProfileDTO lesson = lessonService.getLessonById(id);
            model.addAttribute("lesson", lesson);
            model.addAttribute("allStudents", studentService.getAllActiveStudents());
            model.addAttribute("currencySymbols", getCurrencySymbolsMap());
            List<Long> selectedStudentIds = lesson.getStudentAssociations().stream()
                    .map(LessonStudentDto::getStudentId)
                    .collect(Collectors.toList());
            model.addAttribute("selectedStudentIds", selectedStudentIds);
            return "lesson/edit-lesson";
        }

        lessonService.updateLesson(id, lessonUpdateForm);
        redirectAttributes.addFlashAttribute("message", "Lesson updated successfully!");
        return "redirect:/lessons/profile/{id}";
    }

    @PostMapping("/delete/{id}")
    public String deleteLesson(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        lessonService.deleteLesson(id);
        redirectAttributes.addFlashAttribute("message", "Lesson deleted successfully!");
        return "redirect:/lessons/list";
    }

    private Map<String, String> getCurrencySymbolsMap() {
        return Arrays.stream(Currency.values())
                .collect(Collectors.toMap(Enum::name, Currency::getSymbol));
    }
}
