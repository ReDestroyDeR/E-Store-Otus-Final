package ru.red.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("billings")
public class UserBilling implements Persistable<Long> {
    @Id
    private Long id;
    private String email;
    private Integer balance;

    @Transient
    private boolean isNew = false;

    @Override
    @JsonIgnore
    public boolean isNew() {
        return isNew || id == null;
    }
}
