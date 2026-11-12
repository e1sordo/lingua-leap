package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.Language;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguagesRepository extends ListCrudRepository<Language, String> {
}
