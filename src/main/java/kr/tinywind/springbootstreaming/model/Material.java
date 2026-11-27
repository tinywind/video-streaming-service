package kr.tinywind.springbootstreaming.model;

import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@RequiredArgsConstructor
public class Material extends AbstractPersistable<Long> {
    @Size(min = 1, max = 1024)
    @Column(nullable = false)
    @NonNull
    private String videoName;

    @Basic(optional = false)
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @NonNull
    private Date createdAt = new Date();

    @OneToMany(mappedBy = "material", orphanRemoval = true, cascade = CascadeType.PERSIST)
    private List<MaterialData> materialDataList = new ArrayList<>();

    public void addAnnotation(double timestamp, String key) {
        materialDataList.add(new MaterialData(this, timestamp, key));
    }

    public List<MaterialData> getMaterialDataList() {
        return Collections.unmodifiableList(materialDataList);
    }
}