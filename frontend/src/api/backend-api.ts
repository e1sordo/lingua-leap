import axios, { AxiosResponse } from 'axios';

const axiosApi = axios.create({
    baseURL: `/api`,
    timeout: 8000,
    headers: { 'Content-Type': 'application/json' }
});

const courseBase = (courseId: number) => `/courses/${courseId}`;

export interface KeyValueDto {
    key: string;
    value: string;
}

interface User {
    id: number;
    firstName: string;
    lastName: string;
}

export enum GrammaticalGender {
    MASCULINE,
    FEMININE,
    NEUTER,
    PROPER
}

export enum PartOfSpeech {
    NOUN,
    VERB,
    ADJECTIVE,
    ADVERB,
    PRONOUN,
    PREPOSITION,
    CONJUNCTION,
    INTERJECTION,
    DETERMINER,

    PHRASE
}

export enum LearningStatus {
    NEW,
    TO_STUDY,
    KNOWN
}

export type TranslationMap = { [languageCode: string]: string };

export interface LanguageDto {
    code: string;
    name: string;
    defaultSpeechLocale: string;
}

export interface CourseDto {
    id: number;
    targetLanguageCode: string;
    targetLanguageName: string;
    defaultSpeechLocale: string;
    translationLanguageCodes: string[];
    totalWords: number;
    dueToday: number;
}

export interface CreateCourseRequestDto {
    targetLanguageCode: string;
    translationLanguageCodes: string[];
}

export interface RecentlyAddedForeignWordsPageDto {
    isLast: boolean;
    data: ForeignWordDto[];
}

export interface ForeignWordDto {
    id: number;
    word: string;
    translations: TranslationMap;
    pos: PartOfSpeech | string;
    imageUrl: string | null;
    gender: GrammaticalGender | string | null;
    learningStatus: LearningStatus | string;
    addedDate: Date | null;
}

export interface AddNewWordRequestDto {
    addedDate: Date;
    word: string;
    meanings: WordMeaningDto[];
}

export interface ForeignWordDetailDto {
    id: number;
    word: string;
    addedDate: Date | null;
    meanings: WordMeaningDto[];
}

export interface PartOfSpeechStatisticsDto {
    pos: PartOfSpeech;
    count: number;
}

export interface WordMeaningDto {
    id: number;
    word: string;
    pos: PartOfSpeech | string;
    gender: GrammaticalGender | string | null;
    imageUrl: string | null;
    translations: TranslationMap;
    definition: string;
    frequency: number;
    learningStatus: LearningStatus | string;
    lists: VocabularyListDto[];
    collocations: WordMeaningCollocationDto[];
    contexts: WordMeaningContextDto[];
}

export interface WordMeaningContextDto {
    id: number;
    sentence: string;
    translation: string;
}

export interface WordMeaningCollocationDto {
    id: number;
    pattern: string;
    resolvedPattern: string;
    translations: TranslationMap;
}

export interface VocabularyListDto {
    id: number;
    name: string;
    smart: boolean;
}

export interface VocabularyListDetailDto {
    id: number;
    name: string;
    words: WordMeaningDto[];
}

export interface AddedWordsStatisticsDto {
    summary: DateCountDto[];
    addedThisWeek: number;
    addedPreviousWeek: number;
    addedThisMonth: number;
    addedPreviousMonth: number;
}

export interface DateCountDto {
    date: Date;
    count: number;
}

export interface AddLaterWordDto {
    word: string;
    timesAdded: number;
}

export default {
    heartbeat(): Promise<AxiosResponse<void>> {
        return axiosApi.get('/heartbeat');
    },


    // languages & courses
    getLanguages(): Promise<AxiosResponse<LanguageDto[]>> {
        return axiosApi.get('/languages');
    },

    getCourses(): Promise<AxiosResponse<CourseDto[]>> {
        return axiosApi.get('/courses');
    },

    createCourse(body: CreateCourseRequestDto): Promise<AxiosResponse<CourseDto>> {
        return axiosApi.post('/courses', body);
    },


    // words
    getAllRecentlyAddedWords(
        courseId: number,
        page: number,
        pageSize: number,
        lastLoadedWordId: number = -1,
        rollOut: boolean = false
    ): Promise<AxiosResponse<RecentlyAddedForeignWordsPageDto>> {
        return axiosApi.get(
            `${courseBase(courseId)}/words?rollOut=${rollOut}&page=${page}&pageSize=${pageSize}&startsFromId=${lastLoadedWordId}`
        );
    },

    getAutocompleteSuggestions(courseId: number, term: string): Promise<AxiosResponse<string[]>> {
        return axiosApi.get(courseBase(courseId) + '/words/autosuggestions?word=' + term);
    },

    addNewWord(courseId: number, body: AddNewWordRequestDto): Promise<AxiosResponse<void>> {
        return axiosApi.post(courseBase(courseId) + '/words', body);
    },

    getByName(courseId: number, name: string): Promise<AxiosResponse<ForeignWordDetailDto>> {
        return axiosApi.get(`${courseBase(courseId)}/words/${name}`);
    },

    async getTotalAddedWords(courseId: number): Promise<number> {
        const response = await axiosApi.get(`${courseBase(courseId)}/words/total`);
        return response.data.total;
    },

    getAllAddLaterWords(courseId: number): Promise<AxiosResponse<AddLaterWordDto[]>> {
        return axiosApi.get(courseBase(courseId) + '/words/later');
    },

    deleteWordFromAddLaterList(courseId: number, word: string): Promise<AxiosResponse<void>> {
        return axiosApi.delete(courseBase(courseId) + '/words/later/' + word);
    },

    getAddedWordsStatistics(courseId: number): Promise<AxiosResponse<AddedWordsStatisticsDto>> {
        return axiosApi.get(courseBase(courseId) + '/words/statistics');
    },

    deleteWord(courseId: number, word: string): Promise<AxiosResponse<void>> {
        return axiosApi.delete(`${courseBase(courseId)}/words/${word}`);
    },


    // meanings
    linkContext(courseId: number, meaningId: number, body: WordMeaningContextDto): Promise<AxiosResponse<WordMeaningContextDto>> {
        return axiosApi.post(courseBase(courseId) + '/meanings/' + meaningId + '/contexts', body);
    },

    linkCollocation(courseId: number, meaningId: number, body: WordMeaningCollocationDto): Promise<AxiosResponse<WordMeaningCollocationDto>> {
        return axiosApi.post(courseBase(courseId) + '/meanings/' + meaningId + '/collocations', body);
    },

    editTranslations(courseId: number, meaningId: number, translations: TranslationMap): Promise<AxiosResponse<void>> {
        return axiosApi.patch(courseBase(courseId) + '/meanings/' + meaningId + '/translations', { translations });
    },

    editDefinition(courseId: number, meaningId: number, newValue: string): Promise<AxiosResponse<void>> {
        return axiosApi.patch(courseBase(courseId) + '/meanings/' + meaningId + '/definition', { newValue });
    },

    editImageUrl(courseId: number, meaningId: number, newValue: string): Promise<AxiosResponse<void>> {
        return axiosApi.patch(courseBase(courseId) + '/meanings/' + meaningId + '/image', { newValue });
    },

    editCollocation(courseId: number, meaningId: number, collocationId: number, request: WordMeaningCollocationDto): Promise<AxiosResponse<WordMeaningCollocationDto>> {
        return axiosApi.put(courseBase(courseId) + '/meanings/' + meaningId + '/collocations/' + collocationId, request);
    },

    removeCollocation(courseId: number, meaningId: number, collocationId: number): Promise<AxiosResponse<void>> {
        return axiosApi.delete(courseBase(courseId) + '/meanings/' + meaningId + '/collocations/' + collocationId);
    },

    getMeaningsStatistics(courseId: number): Promise<AxiosResponse<PartOfSpeechStatisticsDto[]>> {
        return axiosApi.get(courseBase(courseId) + '/meanings/statistics');
    },


    // lists
    getAllLists(courseId: number, excludeSmart: boolean = false): Promise<AxiosResponse<VocabularyListDto[]>> {
        const smartParamString = excludeSmart ? '?excludeSmart=true' : '';
        return axiosApi.get(courseBase(courseId) + '/lists' + smartParamString);
    },

    addNewList(courseId: number, name: string): Promise<AxiosResponse<VocabularyListDto>> {
        return axiosApi.post(courseBase(courseId) + '/lists', { name });
    },

    getListById(courseId: number, id: number): Promise<AxiosResponse<VocabularyListDetailDto>> {
        return axiosApi.get(`${courseBase(courseId)}/lists/${id}`);
    },

    getListByPos(courseId: number, pos: string): Promise<AxiosResponse<VocabularyListDetailDto>> {
        return axiosApi.get(`${courseBase(courseId)}/lists/pos/${pos}`);
    },

    getListByDate(courseId: number, date: string): Promise<AxiosResponse<VocabularyListDetailDto>> {
        return axiosApi.get(`${courseBase(courseId)}/lists/date/${date}`);
    },

    removeWordFromList(courseId: number, id: number, meaningId: number): Promise<AxiosResponse<void>> {
        return axiosApi.delete(`${courseBase(courseId)}/lists/${id}/words/${meaningId}`);
    },

    deleteList(courseId: number, id: number): Promise<AxiosResponse<void>> {
        return axiosApi.delete(`${courseBase(courseId)}/lists/${id}`);
    },


    // repetition
    async getTotalWordsToRepeatToday(courseId: number): Promise<number> {
        const response = await axiosApi.get(`${courseBase(courseId)}/repetition/today/total`);
        return response.data.total;
    },

    getPlannedRepetitionsSummaryGraph(courseId: number): Promise<AxiosResponse<DateCountDto[]>> {
        return axiosApi.get(courseBase(courseId) + '/repetition/summary/graph');
    },

    getWordsToRepeatToday(courseId: number): Promise<AxiosResponse<WordMeaningDto[]>> {
        return axiosApi.get(courseBase(courseId) + '/repetition/today');
    },

    scoreWordMeaning(courseId: number, meaningId: number, newScore: number): Promise<AxiosResponse<void>> {
        return axiosApi.put(`${courseBase(courseId)}/repetition/meanings/${meaningId}?newScore=${newScore}`);
    },


    // security
    getUser(userId: number): Promise<AxiosResponse<User>> {
        return axiosApi.get(`/user/` + userId);
    },
    getSecured(user: string, password: string): Promise<AxiosResponse<string>> {
        return axiosApi.get(`/secured/`, {
            auth: {
                username: user,
                password: password
            }
        });
    }
}
