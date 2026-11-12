import { LanguageModule } from './types';

const englishDictionaries = [
    {
        name: 'PROMT.One',
        color: 'success',
        link: (word: string, translationLanguageCode: string) =>
            translationLanguageCode === 'ru'
                ? `https://www.translate.ru/перевод/английский-русский/${word}`
                : null
    },
    {
        name: 'Cambridge',
        color: 'primary',
        link: (word: string) => `https://dictionary.cambridge.org/dictionary/english/${word}`
    },
    {
        name: 'YouGlish',
        color: 'success',
        link: (word: string) => `https://youglish.com/pronounce/${word}/english`
    }
];

export const englishModule: LanguageModule = {
    code: 'en',
    speechLocale: 'en-US',
    nounArticles: {},
    dictionaries: englishDictionaries,
    supportsNumbers: false,
    supportsAbbreviations: true
};
