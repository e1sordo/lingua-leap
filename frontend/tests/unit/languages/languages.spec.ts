import { getLanguageModule } from '@/languages';

describe('language modules', () => {
    it('Spanish module provides EL/LA articles and numbers support', () => {
        const spanish = getLanguageModule('es');

        expect(spanish.speechLocale).toBe('es-ES');
        expect(spanish.nounArticles.MASCULINE).toBe('EL');
        expect(spanish.nounArticles.FEMININE).toBe('LA');
        expect(spanish.supportsNumbers).toBe(true);
        expect(spanish.supportsAbbreviations).toBe(true);
    });

    it('French module provides LE/LA articles without numbers support', () => {
        const french = getLanguageModule('fr');

        expect(french.speechLocale).toBe('fr-FR');
        expect(french.nounArticles.MASCULINE).toBe('LE');
        expect(french.nounArticles.FEMININE).toBe('LA');
        expect(french.supportsNumbers).toBe(false);
    });

    it('English module has no gender articles', () => {
        const english = getLanguageModule('en');

        expect(english.speechLocale).toBe('en-US');
        expect(english.nounArticles.MASCULINE).toBeUndefined();
        expect(english.nounArticles.FEMININE).toBeUndefined();
        expect(english.supportsNumbers).toBe(false);
    });

    it('unknown language falls back to a minimal module', () => {
        const fallback = getLanguageModule('it');

        expect(fallback.code).toBe('it');
        expect(fallback.speechLocale).toBe('it');
        expect(fallback.nounArticles).toEqual({});
        expect(fallback.dictionaries).toEqual([]);
        expect(fallback.supportsNumbers).toBe(false);
        expect(fallback.supportsAbbreviations).toBe(false);
    });

    it('Spanish PROMT link only exists for the Russian pair', () => {
        const spanish = getLanguageModule('es');
        const prompt = spanish.dictionaries.find(service => service.name === 'PROMT.One');

        expect(prompt?.link('mesa', 'ru')).toContain('испанский-русский/mesa');
        expect(prompt?.link('mesa', 'en')).toBeNull();
    });
});
