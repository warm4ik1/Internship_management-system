package org.warm4ik.ims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.warm4ik.ims.entity.enums.ContactType;

import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "contact_info_internships", schema = "todolist")
public class InternshipContactData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "internship_id", nullable = false)
  private Internship internship;

  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "contact_type", nullable = false, columnDefinition = "contact_type")
  private ContactType contactType;

  @Column(name = "contact_value", nullable = false)
  private String contactValue;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InternshipContactData that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
