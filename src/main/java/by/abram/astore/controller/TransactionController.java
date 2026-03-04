package by.abram.astore.controller;

import by.abram.astore.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/without")
    public String withoutTransaction() {
        try {
            transactionService.saveWithoutTransaction();
            return "Операция завершена (частичное сохранение)";
        } catch (Exception e) {
            return "Ошибка: ";
        }
    }

    @GetMapping("/with")
    public String withTransaction() {
        try {
            transactionService.saveWithTransaction();
            return "stop";
        } catch (Exception e) {
            return "Ошибка: ";
        }
    }
}