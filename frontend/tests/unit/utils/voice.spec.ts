import { createFallbackModule } from '@/languages/fallback';
import { getLanguageModule } from '@/languages/index';

const mockVoices = [
    { name: 'Paulina', lang: 'es-ES', voiceURI: 'uri-es-es' },
    { name: 'Carlos', lang: 'es-MX', voiceURI: 'uri-es-mx' },
    { name: 'Alice', lang: 'en-US', voiceURI: 'uri-en-us' },
    { name: 'Jean', lang: 'fr-FR', voiceURI: 'uri-fr-fr' }
];

const mockSynth = {
    getVoices: jest.fn(() => mockVoices),
    speak: jest.fn(),
    cancel: jest.fn(),
    onvoiceschanged: null as null | (() => void)
};

class MockUtterance {
    text = '';
    lang = '';
    voice: SpeechSynthesisVoice | null = null;
    pitch = 1;
    rate = 1;
}

function loadVoiceModule() {
    jest.resetModules();
    (window as unknown as { speechSynthesis: unknown }).speechSynthesis = mockSynth;
    (window as unknown as { SpeechSynthesisUtterance: unknown }).SpeechSynthesisUtterance = MockUtterance;
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    return require('@/utils/voice');
}

describe('voice utils', () => {
    beforeEach(() => {
        localStorage.clear();
        mockSynth.speak.mockClear();
        mockSynth.getVoices.mockClear();
    });

    it('filters voices by the full speech locale first', () => {
        const voice = loadVoiceModule();
        const voices = voice.getVoices(getLanguageModule('es'));

        expect(voices.map((v: SpeechSynthesisVoice) => v.voiceURI)).toEqual(['uri-es-es']);
    });

    it('falls back to the base language when the exact locale is missing', () => {
        const voice = loadVoiceModule();
        const argentinian = createFallbackModule('es-AR');
        const voices = voice.getVoices(argentinian);

        expect(voices.map((v: SpeechSynthesisVoice) => v.voiceURI))
            .toEqual(['uri-es-es', 'uri-es-mx']);
    });

    it('resolves voices for en-US and fr-FR', () => {
        const voice = loadVoiceModule();

        expect(voice.getVoices(getLanguageModule('en'))[0].voiceURI).toBe('uri-en-us');
        expect(voice.getVoices(getLanguageModule('fr'))[0].voiceURI).toBe('uri-fr-fr');
    });

    it('speaks with the module locale and chosen voice', () => {
        const voice = loadVoiceModule();

        voice.speak('hola', getLanguageModule('es'), { voiceIndex: 0, courseId: 7 });

        expect(mockSynth.speak).toHaveBeenCalledTimes(1);
        const utterance = mockSynth.speak.mock.calls[0][0] as MockUtterance;
        expect(utterance.text).toBe('hola');
        expect(utterance.lang).toBe('es-ES');
        expect(utterance.voice?.voiceURI).toBe('uri-es-es');
    });

    it('restores the voice preference saved for the course', () => {
        const voice = loadVoiceModule();

        localStorage.setItem('lingualeap:voice:7', 'uri-en-us');
        voice.speak('hello', getLanguageModule('en'), { courseId: 7 });

        const utterance = mockSynth.speak.mock.calls[0][0] as MockUtterance;
        expect(utterance.voice?.voiceURI).toBe('uri-en-us');
    });

    it('saves and reads voice preference per course', () => {
        const voice = loadVoiceModule();
        const voices = voice.getVoices(getLanguageModule('es'));

        expect(voice.getSavedVoiceIndex(3, voices)).toBe(-1);

        voice.saveVoicePreference(3, voices[0]);
        expect(voice.getSavedVoiceIndex(3, voices)).toBe(0);
        expect(localStorage.getItem('lingualeap:voice:3')).toBe('uri-es-es');
    });
});
