package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.enums.AppSettingKeys;
import es.e1sordo.lingualeap.models.AppSettingEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingsRepository extends ListCrudRepository<AppSettingEntity, AppSettingKeys> {
}
