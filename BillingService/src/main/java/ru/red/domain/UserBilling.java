package ru.red.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("billings")
public class UserBilling {
    @Id
    private Long id;
    private String email;
    private Integer balance;
}
