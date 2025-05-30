package org.warm4ik.ims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.warm4ik.ims.entity.enums.PaymentType;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "internships", schema = "todolist")
public class Internship {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "payment_type", nullable = false, columnDefinition = "payment_type")
  private PaymentType paymentType;

  @OneToMany(
      mappedBy = "internship",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<InternshipContactData> contactData = new ArrayList<>();

  @Column(
      name = "logo_path",
      nullable = false,
      columnDefinition = "text default 'images/default_logo.png'")
  private String logoUrl;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "internship_categories",
      schema = "todolist",
      joinColumns = @JoinColumn(name = "internship_id"),
      inverseJoinColumns = @JoinColumn(name = "category_id"))
  @Builder.Default
  private List<Category> categories = new ArrayList<>();

  @Column(name = "address", nullable = false)
  private String address;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Internship that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  public void addContactData(InternshipContactData contactDataItem) {
    // Добавление контакта в список
    this.contactData.add(contactDataItem);
    // Установка обратной ссылки
    contactDataItem.setInternship(this);
  }

  public void clearContactData() {
    if (contactData != null) {
      for (InternshipContactData contact : contactData) {
        contact.setInternship(null);
      }
      contactData.clear();
    }
  }
}
