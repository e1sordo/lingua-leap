package es.e1sordo.lingualeap.dto;

import es.e1sordo.lingualeap.enums.AppSettingKeys;

public record AppSettingDto(AppSettingKeys key, String value) {
}
