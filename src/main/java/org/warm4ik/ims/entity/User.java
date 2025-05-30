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
@Table(name = "users", schema = "todolist")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "username")
  private String username;

  @Column(name = "password")
  private String password;

  @Column(name = "email")
  private String email;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      schema = "todolist",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private List<Role> roles;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_favorites",
      schema = "todolist",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "internship_id"))
  private List<Internship> favoriteInternships;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User user)) return false;
    return Objects.equals(id, user.id) && Objects.equals(username, user.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username);
  }
}
