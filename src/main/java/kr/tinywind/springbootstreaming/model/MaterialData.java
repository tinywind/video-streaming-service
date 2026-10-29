package kr.tinywind.springbootstreaming.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.AbstractPersistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialData extends AbstractPersistable<Long> {
    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Material material;

    @Column(nullable = false)
    @NonNull
    private Double timestamp;

    @Column(nullable = false)
    @NonNull
    private String key;
}