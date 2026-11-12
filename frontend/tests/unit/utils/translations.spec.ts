import { CourseDto } from '@/api/backend-api';
import {
    languageLabel,
    normalizeTranslations,
    orderedTranslations,
    primaryTranslation,
    translation
} from '@/utils/translations';

const course: CourseDto = {
    id: 1,
    targetLanguageCode: 'es',
    targetLanguageName: 'Spanish',
    defaultSpeechLocale: 'es-ES',
    translationLanguageCodes: ['ru', 'en'],
    totalWords: 0,
    dueToday: 0
};

const item = {
    translations: { en: 'hello', ru: 'привет' }
};

describe('translations utils', () => {
    it('translation returns the value for the requested language', () => {
        expect(translation(item, 'ru')).toBe('привет');
        expect(translation(item, 'fr')).toBe('');
    });

    it('primaryTranslation uses the first translation language of the course', () => {
        expect(primaryTranslation(item, course)).toBe('привет');

        const enCourse = { ...course, translationLanguageCodes: ['en', 'ru'] };
        expect(primaryTranslation(item, enCourse)).toBe('hello');
    });

    it('orderedTranslations follows the course order, not the object key order', () => {
        const ordered = orderedTranslations(item, course);
        expect(ordered.map(entry => entry.code)).toEqual(['ru', 'en']);
        expect(ordered[0].text).toBe('привет');
        expect(ordered[1].text).toBe('hello');
    });

    it('orderedTranslations skips missing translations', () => {
        const partial = { translations: { ru: 'привет' } };
        const ordered = orderedTranslations(partial, course);
        expect(ordered).toHaveLength(1);
        expect(ordered[0].code).toBe('ru');
    });

    it('normalizeTranslations trims and drops empty values', () => {
        expect(normalizeTranslations({ ru: '  привет ', en: '   ', fr: '' }))
            .toEqual({ ru: 'привет' });
    });

    it('languageLabel falls back to the code', () => {
        const languages = [
            { code: 'ru', name: 'Russian', defaultSpeechLocale: 'ru-RU' }
        ];
        expect(languageLabel('ru', languages)).toBe('Russian');
        expect(languageLabel('es', languages)).toBe('es');
    });
});
