import api from '@/api/backend-api';
import ListenAbbreviationsView from '@/views/study/ListenAbbreviationsView.vue';
import { useCourses } from '@/composables/useCourses';
import { flushPromises, mount } from '@vue/test-utils';

jest.mock('@/api/backend-api', () => ({
    __esModule: true,
    default: {
        getLanguages: jest.fn(() => Promise.resolve({ data: [] })),
        getListByPos: jest.fn()
    }
}));

const esCourse = {
    id: 1,
    targetLanguageCode: 'es',
    targetLanguageName: 'Spanish',
    defaultSpeechLocale: 'es-ES',
    translationLanguageCodes: ['ru'],
    totalWords: 0,
    dueToday: 0
};

const mountView = () => mount(ListenAbbreviationsView, {
    global: { mocks: { $t: (key: string) => key } }
});

describe('ListenAbbreviationsView', () => {
    beforeEach(() => {
        const { state } = useCourses();
        state.courses = [esCourse];
        state.activeCourseId = 1;
        jest.clearAllMocks();
    });

    it('shows an empty state when the course has no abbreviations', async () => {
        (api.getListByPos as jest.Mock).mockResolvedValue({ data: { id: 1, name: 'A', words: [] } });

        const wrapper = mountView();
        await flushPromises();

        expect(api.getListByPos).toHaveBeenCalledWith(1, 'ABBREVIATION');
        expect(wrapper.text()).toContain('study.noAbbreviations');
    });

    it('works with a single abbreviation without looping', async () => {
        (api.getListByPos as jest.Mock).mockResolvedValue({
            data: {
                id: 1,
                name: 'A',
                words: [{
                    id: 10,
                    word: 'ABC',
                    pos: 'ABBREVIATION',
                    gender: null,
                    imageUrl: null,
                    translations: {},
                    definition: '',
                    frequency: 1,
                    learningStatus: 'KNOWN',
                    lists: [],
                    collocations: [],
                    contexts: []
                }]
            }
        });

        const wrapper = mountView();
        await flushPromises();

        expect((wrapper.vm as any).totalVariants).toBe(1);

        (wrapper.vm as any).startTraining();
        expect((wrapper.vm as any).currentAbbreviation).toBe('ABC');
    });
});
