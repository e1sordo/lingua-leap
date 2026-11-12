import { GrammaticalGender } from '@/api/backend-api';

export interface DictionaryServiceFactory {
    name: string;
    color: string;
    link: (word: string, translationLanguageCode: string) => string | null;
}

export interface LanguageModule {
    code: string;
    speechLocale: string;
    nounArticles: Partial<Record<keyof typeof GrammaticalGender, string>>;
    dictionaries: DictionaryServiceFactory[];
    supportsNumbers: boolean;
    supportsAbbreviations: boolean;
}
