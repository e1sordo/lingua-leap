package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.enums.AppSettingKeys;
import es.e1sordo.lingualeap.models.AppSettingEntity;

public interface AppSettingsService {

    void set(AppSettingKeys key, String value);

    AppSettingEntity get(AppSettingKeys key);
}
