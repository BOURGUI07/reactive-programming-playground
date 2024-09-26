package com.example.reactive_sec1.assignments.assignment5.vinoth;

import java.time.LocalDate;
import java.util.Map;

public record RevenueReport(
        LocalDate date,
        Map<String, Integer> revenue
) {
}
