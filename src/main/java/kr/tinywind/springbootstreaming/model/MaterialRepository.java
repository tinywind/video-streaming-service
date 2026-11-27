package kr.tinywind.springbootstreaming.model;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    @Override
    @EntityGraph(attributePaths = "materialDataList")
    Optional<Material> findById(Long id);
}