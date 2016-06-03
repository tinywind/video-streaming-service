package kr.tinywind.springbootstreaming.model;

import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
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

    @OneToMany(mappedBy = "material", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private List<MaterialData> materialDataList = new MaterialDataList<>(this);
}