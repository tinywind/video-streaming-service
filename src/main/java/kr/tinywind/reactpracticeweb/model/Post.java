package kr.tinywind.reactpracticeweb.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Data
public class Post {
    @Id
    @GeneratedValue
    private Long id;

    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String title;

    @Size(max = 255)
    private String subtitle;

    @Size(min = 1, max = 100000000)
    @Column(length = 100000000, nullable = false)
    private String content;

    @Basic(optional = false)
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createdAt;
}