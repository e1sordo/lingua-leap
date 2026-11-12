import api from '@/api/backend-api';
import { useCourses } from '@/composables/useCourses';
import WordFormView from '@/views/WordFormView.vue';
import { flushPromises, mount } from '@vue/test-utils';

jest.mock('@/i18n', () => ({
    __esModule: true,
    default: { global: { t: (key: string) => key } }
}));

jest.mock('vue-multiselect', () => ({
    __esModule: true,
    default: { name: 'VueMultiselect', template: '<div />' }
}));

jest.mock('@/api/backend-api', () => ({
    __esModule: true,
    default: {
        getAllLists: jest.fn(() => Promise.resolve({ data: [] })),
        getLanguages: jest.fn(() => Promise.resolve({
            data: [{ code: 'ru', name: 'Russian', defaultSpeechLocale: 'ru-RU' }]
        })),
        addNewWord: jest.fn(() => Promise.resolve({}))
    }
}));

const enCourse = {
    id: 1,
    targetLanguageCode: 'en',
    targetLanguageName: 'English',
    defaultSpeechLocale: 'en-US',
    translationLanguageCodes: ['ru'],
    totalWords: 0,
    dueToday: 0
};

describe('WordFormView', () => {
    beforeEach(() => {
        const { state } = useCourses();
        state.courses = [enCourse];
        state.activeCourseId = 1;
        jest.clearAllMocks();
    });

    it('renders a required primary translation field for EN+[RU] without an EN field', async () => {
        const wrapper = mount(WordFormView, {
            global: { mocks: { $route: { query: {} } } }
        });
        await flushPromises();

        const primaryInput = wrapper.find('#translation-ru');
        expect(primaryInput.exists()).toBe(true);
        expect(primaryInput.attributes('required')).toBeDefined();
        expect(wrapper.find('#translation-en').exists()).toBe(false);
    });

    it('submits normalized translations with the active course id', async () => {
        const wrapper = mount(WordFormView, {
            global: { mocks: { $route: { query: {} } } }
        });
        await flushPromises();

        await wrapper.find('#word').setValue('table');
        await wrapper.find('#translation-ru').setValue('  стол  ');
        await wrapper.find('form').trigger('submit.prevent');
        await flushPromises();

        expect(api.addNewWord).toHaveBeenCalledTimes(1);
        expect(api.addNewWord).toHaveBeenCalledWith(1, expect.objectContaining({
            word: 'table',
            meanings: [expect.objectContaining({ translations: { ru: 'стол' } })]
        }));
    });
});
