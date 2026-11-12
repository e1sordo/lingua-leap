import { getLanguageModule, LanguageModule } from '@/languages';

interface SpeakOptions {
    voiceIndex?: number;
    voiceModificationCheck?: boolean;
    courseId?: number;
}

let cachedVoices: SpeechSynthesisVoice[] = [];
const voicesChangedListeners = new Set<() => void>();

function hasSpeechSynthesis(): boolean {
    return typeof window !== 'undefined' && !!window.speechSynthesis;
}

function refreshVoices() {
    if (!hasSpeechSynthesis()) {
        return;
    }
    cachedVoices = window.speechSynthesis.getVoices();
    voicesChangedListeners.forEach(listener => listener());
}

if (hasSpeechSynthesis()) {
    refreshVoices();
    window.speechSynthesis.onvoiceschanged = refreshVoices;
}

export function getVoices(languageModule: LanguageModule): SpeechSynthesisVoice[] {
    const fullLocaleVoices = cachedVoices.filter(voice => voice.lang === languageModule.speechLocale);
    if (fullLocaleVoices.length > 0) {
        return fullLocaleVoices;
    }

    const baseLanguage = languageModule.speechLocale.split('-')[0];
    return cachedVoices.filter(voice =>
        voice.lang === baseLanguage || voice.lang.startsWith(baseLanguage + '-')
    );
}

export function onVoicesChanged(listener: () => void): () => void {
    voicesChangedListeners.add(listener);
    return () => voicesChangedListeners.delete(listener);
}

function voiceStorageKey(courseId: number): string {
    return `lingualeap:voice:${courseId}`;
}

export function saveVoicePreference(courseId: number, voice: SpeechSynthesisVoice): void {
    localStorage.setItem(voiceStorageKey(courseId), voice.voiceURI);
}

export function getSavedVoiceIndex(courseId: number, voices: SpeechSynthesisVoice[]): number {
    const savedUri = localStorage.getItem(voiceStorageKey(courseId));
    if (!savedUri) {
        return -1;
    }

    return voices.findIndex(voice => voice.voiceURI === savedUri);
}

function resolveVoice(
    module: LanguageModule,
    options: SpeakOptions,
    voices: SpeechSynthesisVoice[]
): SpeechSynthesisVoice | null {
    if (voices.length === 0) {
        return null;
    }

    if (options.voiceIndex !== undefined && options.voiceIndex >= 0 && voices[options.voiceIndex]) {
        return voices[options.voiceIndex];
    }

    if (options.courseId !== undefined) {
        const savedIndex = getSavedVoiceIndex(options.courseId, voices);
        if (savedIndex >= 0) {
            return voices[savedIndex];
        }
    }

    return voices[0];
}

export const speak = (
    text: number | string,
    languageModuleOrCode: LanguageModule | string,
    options: SpeakOptions = {}
) => {
    if (!hasSpeechSynthesis()) {
        return;
    }

    const module = typeof languageModuleOrCode === 'string'
        ? getLanguageModule(languageModuleOrCode)
        : languageModuleOrCode;

    const speech = new window.SpeechSynthesisUtterance();
    speech.text = text.toString();
    speech.lang = module.speechLocale;

    const voices = getVoices(module);
    const voice = resolveVoice(module, options, voices);
    if (voice) {
        speech.voice = voice;
    }

    if (options.voiceModificationCheck) {
        speech.pitch = Math.random() * (1.5 - 0.7) + 0.7;
        speech.rate = Math.random() * (1.3 - 0.9) + 0.9;
    }

    window.speechSynthesis.speak(speech);
};
