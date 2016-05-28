package kr.tinywind.reactpracticeweb.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Data
public class Post {
    @Id
    @GeneratedValue
    Long id;

    @Size(min = 1, max = 255)
    @Column(nullable = false)
    String title;

    @Size(max = 255)
    String subtitle;

    @Size(min = 1, max = 100000000)
    @Column(length = 100000000, nullable = false)
    String content;

    @Basic(optional = false)
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date createdAt;
}