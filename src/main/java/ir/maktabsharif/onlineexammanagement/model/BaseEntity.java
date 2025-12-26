package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@SuperBuilder
public abstract class BaseEntity<ID> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BaseGenerator")
    @SequenceGenerator(name = "BaseGenerator", sequenceName = "item-seq", allocationSize = 1)
    private ID id;

    private Instant createdAt;
    private Instant updatedAt;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void OnUpdate() {
        updatedAt = Instant.now();
    }

}