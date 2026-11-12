import { CourseDto, LanguageDto, TranslationMap } from '@/api/backend-api';

export interface TranslatedItem {
    translations: TranslationMap;
}

export function translation(item: TranslatedItem | null | undefined, languageCode: string): string {
    return item?.translations?.[languageCode] || '';
}

export function primaryTranslation(item: TranslatedItem | null | undefined, course: CourseDto | null | undefined): string {
    if (!course || course.translationLanguageCodes.length === 0) {
        return '';
    }

    return translation(item, course.translationLanguageCodes[0]);
}

export function orderedTranslations(
    item: TranslatedItem | null | undefined,
    course: CourseDto | null | undefined
): Array<{ code: string; text: string }> {
    if (!course) {
        return [];
    }

    return course.translationLanguageCodes
        .map(code => ({ code, text: translation(item, code) }))
        .filter(entry => entry.text.length > 0);
}

export function normalizeTranslations(input: TranslationMap): TranslationMap {
    return Object.fromEntries(
        Object.entries(input)
            .map(([code, text]) => [code, (text || '').trim()])
            .filter(([, text]) => text.length > 0)
    );
}

export function languageLabel(code: string, languages: LanguageDto[] | null | undefined): string {
    return languages?.find(language => language.code === code)?.name || code;
}
