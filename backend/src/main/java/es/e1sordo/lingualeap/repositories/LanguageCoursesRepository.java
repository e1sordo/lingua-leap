package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.LanguageCourse;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageCoursesRepository extends ListCrudRepository<LanguageCourse, Long> {
}
