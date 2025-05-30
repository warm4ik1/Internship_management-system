package org.warm4ik.ims.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.warm4ik.ims.entity.Internship;
import org.warm4ik.ims.entity.enums.PaymentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class InternshipSpecification {

  public static Specification<Internship> byMode(String mode, String str) {
    return (root, cq, cb) -> {
      if ("title".equalsIgnoreCase(mode)) {
        return cb.like(cb.lower(root.get("title")), "%" + str.toLowerCase() + "%");
      } else {
        return cb.like(cb.lower(root.get("description")), "%" + str.toLowerCase() + "%");
      }
    };
  }

  public static Specification<Internship> betweenData(LocalDateTime start, LocalDateTime end) {
    return (root, cq, cb) -> {
      Predicate predicate = cb.conjunction();

      if (start != null) {
        predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), start));
      }

      if (end != null) {
        predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), end));
      }

      return predicate;
    };
  }

  public static Specification<Internship> hasAddress(String address) {
    return (root, cq, cb) -> {
      if (address == null || address.isBlank()) {
        return cb.conjunction();
      }
      return cb.equal(root.get("address"), address);
    };
  }

  public static Specification<Internship> hasCategories(List<String> categoryNames) {
    return (root, cq, cb) -> {
      if (categoryNames == null || categoryNames.isEmpty()) {
        return cb.conjunction();
      }

      Objects.requireNonNull(cq).distinct(true);

      return cb.lower(root.join("categories").get("title"))
          .in(categoryNames.stream().map(String::toLowerCase).toList());
    };
  }

  public static Specification<Internship> hasPaymentType(PaymentType paymentType) {
    return (root, cq, cb) -> {
      if (paymentType == null) {
        return cb.conjunction();
      }

      return cb.equal(root.get("paymentType"), paymentType);
    };
  }
}
