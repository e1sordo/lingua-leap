import { englishModule } from './english';
import { createFallbackModule } from './fallback';
import { frenchModule } from './french';
import { spanishModule } from './spanish';
import { LanguageModule } from './types';

const registry = new Map<string, LanguageModule>([
    ['es', spanishModule],
    ['en', englishModule],
    ['fr', frenchModule]
]);

export function getLanguageModule(code: string): LanguageModule {
    return registry.get(code) || createFallbackModule(code);
}

export type { DictionaryServiceFactory, LanguageModule } from './types';
