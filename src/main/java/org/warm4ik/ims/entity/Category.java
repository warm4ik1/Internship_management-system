package org.warm4ik.ims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "categories", schema = "todolist")
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "title")
  private String title;

  @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
  private List<Internship> internships;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Category category)) return false;
    return Objects.equals(id, category.id) && Objects.equals(title, category.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title);
  }
}
