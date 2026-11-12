import { LanguageModule } from './types';

const frenchDictionaries = [
    {
        name: 'PROMT.One',
        color: 'success',
        link: (word: string, translationLanguageCode: string) =>
            translationLanguageCode === 'ru'
                ? `https://www.translate.ru/перевод/французский-русский/${word}`
                : null
    },
    {
        name: 'Larousse',
        color: 'primary',
        link: (word: string) => `https://www.larousse.fr/dictionnaires/francais/${word}`
    },
    {
        name: 'YouGlish',
        color: 'success',
        link: (word: string) => `https://youglish.com/pronounce/${word}/french`
    }
];

export const frenchModule: LanguageModule = {
    code: 'fr',
    speechLocale: 'fr-FR',
    nounArticles: {
        MASCULINE: 'LE',
        FEMININE: 'LA'
    },
    dictionaries: frenchDictionaries,
    supportsNumbers: false,
    supportsAbbreviations: true
};
