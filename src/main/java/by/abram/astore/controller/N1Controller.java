package by.abram.astore.controller;

import by.abram.astore.service.N1Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class N1Controller {

    private final N1Service n1Service;

    @GetMapping("/n1-problem")
    public String showN1Problem() {
        n1Service.showN1Problem();
        return " демонстрация N+1 проблемы";
    }

    @GetMapping("/n1-solution")
    public String solveN1Problem() {
        n1Service.solveN1Problem();
        return " решение N+1 через @EntityGraph";
    }
}