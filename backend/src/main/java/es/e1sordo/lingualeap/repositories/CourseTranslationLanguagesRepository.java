package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.CourseTranslationLanguage;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseTranslationLanguagesRepository extends ListCrudRepository<CourseTranslationLanguage, Long> {
}
