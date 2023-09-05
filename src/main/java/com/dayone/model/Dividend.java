package com.dayone.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Dividend {
    private LocalDateTime date;
    private String dividend;
}
