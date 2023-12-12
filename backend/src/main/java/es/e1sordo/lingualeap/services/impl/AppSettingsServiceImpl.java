package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.enums.AppSettingKeys;
import es.e1sordo.lingualeap.models.AppSettingEntity;
import es.e1sordo.lingualeap.repositories.AppSettingsRepository;
import es.e1sordo.lingualeap.services.AppSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppSettingsServiceImpl implements AppSettingsService {

    private final AppSettingsRepository repository;
//    private final @Lazy CalendarService calendarService;

    @Override
    public void set(final AppSettingKeys key, final String value) {
        repository.save(new AppSettingEntity(key, value));
        handle(key, value);
    }

    private void handle(final AppSettingKeys key, final String value) {
//        if (key == AppSettingKeys.WORK_SINCE) {
//            calendarService.updateNewFirstWorkingDay(null);
//        }
    }

    @Override
    public AppSettingEntity get(final AppSettingKeys key) {
        return repository.findById(key).orElse(null);
    }
}
