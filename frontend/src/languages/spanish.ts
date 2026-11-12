import { LanguageModule } from './types';

const spanishDictionaries = [
    {
        name: 'PROMT.One',
        color: 'success',
        link: (word: string, translationLanguageCode: string) =>
            translationLanguageCode === 'ru'
                ? `https://www.translate.ru/перевод/испанский-русский/${word}`
                : null
    },
    {
        name: 'Span¡shD!ctionary',
        color: 'primary',
        link: (word: string) => `https://www.spanishdict.com/translate/${word}`
    },
    {
        name: 'YouGlish',
        color: 'success',
        link: (word: string) => `https://youglish.com/pronounce/${word}/spanish`
    },
    {
        name: 'RAE',
        color: 'primary',
        link: (word: string) => `https://dle.rae.es/${word}`
    },
    {
        name: 'bab.la',
        color: 'primary',
        link: (word: string) => `https://es.bab.la/diccionario/espanol/${word}`
    },
    {
        name: 'linguatools.de',
        color: 'warning',
        link: (word: string) =>
            `https://www.linguatools.de/kollokationen-es/bolls/search?utf8=✓&query=${word}&commit=Search+Collocations%21`
    }
];

export const spanishModule: LanguageModule = {
    code: 'es',
    speechLocale: 'es-ES',
    nounArticles: {
        MASCULINE: 'EL',
        FEMININE: 'LA'
    },
    dictionaries: spanishDictionaries,
    supportsNumbers: true,
    supportsAbbreviations: true
};
