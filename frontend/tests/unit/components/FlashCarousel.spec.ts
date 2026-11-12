import api from '@/api/backend-api';
import FlashCarousel from '@/components/FlashCarousel.vue';
import { useCourses } from '@/composables/useCourses';
import { flushPromises, mount } from '@vue/test-utils';

jest.mock('@/api/backend-api', () => ({
    __esModule: true,
    default: {
        getLanguages: jest.fn(() => Promise.resolve({ data: [] })),
        scoreWordMeaning: jest.fn(() => Promise.resolve({}))
    }
}));

const esCourse = {
    id: 1,
    targetLanguageCode: 'es',
    targetLanguageName: 'Spanish',
    defaultSpeechLocale: 'es-ES',
    translationLanguageCodes: ['ru', 'en'],
    totalWords: 0,
    dueToday: 0
};

const meaning = {
    id: 5,
    word: 'hola',
    pos: 'INTERJECTION',
    gender: 'MASCULINE',
    imageUrl: null,
    translations: { ru: 'привет', en: 'hello' },
    definition: '',
    frequency: 1,
    learningStatus: 'NEW',
    lists: [],
    collocations: [],
    contexts: []
};

const mountCarousel = () => mount(FlashCarousel, {
    props: {
        meaning,
        onSubmit: jest.fn()
    },
    global: {
        mocks: { $t: (key: string) => key },
        stubs: {
            'router-link': true,
            'word-context-menu': { template: '<div><slot /></div>' }
        },
        provide: {
            decreaseTotalWordsToRepeatToday: () => { }
        }
    }
});

// jsdom does not implement scrollIntoView
if (!Element.prototype.scrollIntoView) {
    Element.prototype.scrollIntoView = jest.fn();
}

describe('FlashCarousel', () => {
    beforeEach(() => {
        const { state } = useCourses();
        state.courses = [esCourse];
        state.activeCourseId = 1;
        state.languages = [];
        state.languagesLoaded = false;
        jest.clearAllMocks();
    });

    it('shows the primary translation as the question', async () => {
        const wrapper = mountCarousel();
        await flushPromises();

        expect(wrapper.find('.card-body h1').text()).toBe('привет');
    });

    it('reveals the word, Spanish article and ordered course translations on the answer side', async () => {
        const wrapper = mountCarousel();
        await flushPromises();

        await wrapper.find('a').trigger('click');

        expect(wrapper.text()).toContain('hola');
        expect(wrapper.text()).toContain('EL');
        expect(wrapper.text()).toContain('привет');
        expect(wrapper.text()).toContain('hello');
    });

    it('scores through the active course', async () => {
        const wrapper = mountCarousel();
        await flushPromises();

        await wrapper.find('a').trigger('click');
        await wrapper.find('.card-footer .btn-outline-success').trigger('click');
        await flushPromises();

        expect(api.scoreWordMeaning).toHaveBeenCalledWith(1, 5, 5);
    });
});
