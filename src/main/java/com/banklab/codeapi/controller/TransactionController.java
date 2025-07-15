package com.banklab.codeapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransactionController {

    @GetMapping("/transaction")
    public String showTransactionForm(Model model) {

        return "transaction"; // /WEB-INF/views/transaction.jsp
    }
}
