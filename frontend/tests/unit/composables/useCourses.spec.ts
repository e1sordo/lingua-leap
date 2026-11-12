import api from '@/api/backend-api';
import { useCourses } from '@/composables/useCourses';

jest.mock('@/api/backend-api', () => ({
    __esModule: true,
    default: {
        getCourses: jest.fn(),
        getLanguages: jest.fn(),
        createCourse: jest.fn()
    }
}));

const courseEs = {
    id: 1,
    targetLanguageCode: 'es',
    targetLanguageName: 'Spanish',
    defaultSpeechLocale: 'es-ES',
    translationLanguageCodes: ['ru', 'en'],
    totalWords: 10,
    dueToday: 2
};

const courseEn = {
    id: 2,
    targetLanguageCode: 'en',
    targetLanguageName: 'English',
    defaultSpeechLocale: 'en-US',
    translationLanguageCodes: ['ru'],
    totalWords: 3,
    dueToday: 0
};

describe('useCourses', () => {
    beforeEach(() => {
        const { state } = useCourses();
        state.courses = [];
        state.activeCourseId = null;
        state.loaded = false;
        state.loading = null;
        state.error = null;
        localStorage.clear();
        jest.clearAllMocks();
    });

    it('uses the saved course id when it exists', async () => {
        localStorage.setItem('lingualeap:activeCourseId', '2');
        (api.getCourses as jest.Mock).mockResolvedValue({ data: [courseEs, courseEn] });

        await useCourses().loadCourses();

        expect(useCourses().state.activeCourseId).toBe(2);
    });

    it('falls back to the first course when the saved id is invalid', async () => {
        localStorage.setItem('lingualeap:activeCourseId', '999');
        (api.getCourses as jest.Mock).mockResolvedValue({ data: [courseEs, courseEn] });

        await useCourses().loadCourses();

        expect(useCourses().state.activeCourseId).toBe(1);
    });

    it('leaves activeCourseId null when there are no courses', async () => {
        (api.getCourses as jest.Mock).mockResolvedValue({ data: [] });

        await useCourses().loadCourses();

        expect(useCourses().state.activeCourseId).toBeNull();
        expect(useCourses().hasCourses.value).toBe(false);
    });

    it('selectCourse writes the id into localStorage', () => {
        const { state, selectCourse } = useCourses();
        state.courses = [courseEs, courseEn];

        expect(selectCourse(2)).toBe(true);
        expect(state.activeCourseId).toBe(2);
        expect(localStorage.getItem('lingualeap:activeCourseId')).toBe('2');

        expect(selectCourse(555)).toBe(false);
        expect(state.activeCourseId).toBe(2);
    });

    it('createCourse appends the course and activates it', async () => {
        const { state, createCourse } = useCourses();
        state.courses = [courseEs];
        state.activeCourseId = 1;

        (api.createCourse as jest.Mock).mockResolvedValue({ data: courseEn });

        await createCourse({ targetLanguageCode: 'en', translationLanguageCodes: ['ru'] });

        expect(state.courses).toHaveLength(2);
        expect(state.activeCourseId).toBe(2);
        expect(localStorage.getItem('lingualeap:activeCourseId')).toBe('2');
    });
});
