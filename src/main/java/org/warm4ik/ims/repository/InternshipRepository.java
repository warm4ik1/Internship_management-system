package org.warm4ik.ims.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.warm4ik.ims.entity.Internship;

import java.util.Optional;

@Repository
public interface InternshipRepository
    extends JpaRepository<Internship, Long>, JpaSpecificationExecutor<Internship> {

  @NonNull
  Optional<Internship> findById(@NonNull Long id);

  @Query("SELECT i FROM User u JOIN u.favoriteInternships i WHERE u.id = :userId")
  Page<Internship> findFavoritesByUserId(@Param("userId") Long userId, Pageable pageable);
}
