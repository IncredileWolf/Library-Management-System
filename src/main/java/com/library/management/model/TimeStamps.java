package com.library.management.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;


@Getter
@Setter
@MappedSuperclass
public class TimeStamps {
    @CreationTimestamp
    protected Date createdOn;
    @UpdateTimestamp
    protected Date updatedOn;

}
