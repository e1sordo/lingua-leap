import { LanguageModule } from './types';

export function createFallbackModule(code: string): LanguageModule {
    return {
        code,
        speechLocale: code,
        nounArticles: {},
        dictionaries: [],
        supportsNumbers: false,
        supportsAbbreviations: false
    };
}
