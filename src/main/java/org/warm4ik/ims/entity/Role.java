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
@Table(name = "roles", schema = "todolist")
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "title")
  private String title;

  @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
  private List<User> users;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Role role)) return false;
    return Objects.equals(id, role.id) && Objects.equals(title, role.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title);
  }
}
