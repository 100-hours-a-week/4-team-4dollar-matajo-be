package org.ktb.matajo.repository;

import org.ktb.matajo.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag,Long> {
    //태그 이름으로 태그 찾기
    Optional<Tag> findByTagName(String tagName);

    //태그 이름 포함 검색 (검색 기능)
    List<Tag> findByTagNameContaining(String keyword);
}
