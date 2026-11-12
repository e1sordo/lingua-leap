import axios from 'axios';
import api from '@/api/backend-api';

jest.mock('axios', () => {
    const mockAxiosInstance = {
        get: jest.fn(() => Promise.resolve({ data: {} })),
        post: jest.fn(() => Promise.resolve({ data: {} })),
        patch: jest.fn(() => Promise.resolve({ data: {} })),
        put: jest.fn(() => Promise.resolve({ data: {} })),
        delete: jest.fn(() => Promise.resolve({ data: {} }))
    };

    return {
        __esModule: true,
        default: {
            create: jest.fn(() => mockAxiosInstance)
        },
        create: jest.fn(() => mockAxiosInstance)
    };
});

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const instance = (axios.create as jest.Mock)() as any;

describe('backend-api course-scoped URLs', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('getCourses hits /courses', () => {
        api.getCourses();
        expect(instance.get).toHaveBeenCalledWith('/courses');
    });

    it('getLanguages hits /languages', () => {
        api.getLanguages();
        expect(instance.get).toHaveBeenCalledWith('/languages');
    });

    it('createCourse posts to /courses', () => {
        const body = { targetLanguageCode: 'en', translationLanguageCodes: ['ru'] };
        api.createCourse(body);
        expect(instance.post).toHaveBeenCalledWith('/courses', body);
    });

    it('addNewWord posts to the course words endpoint', () => {
        const body = { word: 'table', addedDate: new Date(), meanings: [] };
        api.addNewWord(7, body as never);
        expect(instance.post).toHaveBeenCalledWith('/courses/7/words', body);
    });

    it('getAllRecentlyAddedWords builds a course-scoped query', () => {
        api.getAllRecentlyAddedWords(7, 2, 100, 55, true);
        expect(instance.get).toHaveBeenCalledWith(
            '/courses/7/words?rollOut=true&page=2&pageSize=100&startsFromId=55'
        );
    });

    it('getByName builds a course-scoped URL', () => {
        api.getByName(3, 'mesa');
        expect(instance.get).toHaveBeenCalledWith('/courses/3/words/mesa');
    });

    it('editTranslations patches the translations endpoint', () => {
        api.editTranslations(3, 42, { ru: 'стол' });
        expect(instance.patch).toHaveBeenCalledWith(
            '/courses/3/meanings/42/translations', { translations: { ru: 'стол' } }
        );
    });

    it('getAllLists appends the excludeSmart flag', () => {
        api.getAllLists(3, true);
        expect(instance.get).toHaveBeenCalledWith('/courses/3/lists?excludeSmart=true');
    });

    it('scoreWordMeaning puts to the course repetition endpoint', () => {
        api.scoreWordMeaning(5, 9, 4);
        expect(instance.put).toHaveBeenCalledWith('/courses/5/repetition/meanings/9?newScore=4');
    });

    it('getTotalWordsToRepeatToday reads the total field', async () => {
        instance.get.mockResolvedValueOnce({ data: { total: 13 } });
        await expect(api.getTotalWordsToRepeatToday(5)).resolves.toBe(13);
        expect(instance.get).toHaveBeenCalledWith('/courses/5/repetition/today/total');
    });

    it('getTotalAddedWords reads the total field', async () => {
        instance.get.mockResolvedValueOnce({ data: { total: 99 } });
        await expect(api.getTotalAddedWords(5)).resolves.toBe(99);
        expect(instance.get).toHaveBeenCalledWith('/courses/5/words/total');
    });

    it('heartbeat and security endpoints stay unscoped', () => {
        api.heartbeat();
        expect(instance.get).toHaveBeenCalledWith('/heartbeat');

        api.getSecured('u', 'p');
        expect(instance.get).toHaveBeenCalledWith('/secured/', {
            auth: { username: 'u', password: 'p' }
        });
    });
});
