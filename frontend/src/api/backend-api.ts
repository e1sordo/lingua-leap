import axios, { AxiosResponse } from 'axios';

const axiosApi = axios.create({
    baseURL: `/api`,
    timeout: 8000,
    headers: { 'Content-Type': 'application/json' }
});

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

export interface RecentlyAddedForeignWordsPageDto {
    isLast: boolean;
    data: ForeignWordDto[];
}

export interface ForeignWordDto {
    id: number;
    word: string;
    russianVariant: string;
    englishVariant: string;
    pos: PartOfSpeech | string;
    imageUrl: string;
    gender: GrammaticalGender | string | null;
    learningStatus: LearningStatus | string;
    addedDate: Date | null;
}

export interface AddNewWordRequestDto {
    word: string;
    addedDate: Date;
    meanings: WordMeaningDto[];
}

export interface ForeignWordDetailDto extends AddNewWordRequestDto {
}

export interface PartOfSpeechStatisticsDto {
    pos: PartOfSpeech;
    count: number;
}

export interface WordMeaningDto extends ForeignWordDto {
    definition: string;
    frequency: number;
    collocations: WordMeaningCollocationDto[];
    contexts: WordMeaningContextDto[];
    lists: VocabularyListDto[];
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
    translationRussian: string;
    translationEnglish: string;
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

export interface DateCountDto {
    date: Date;
    count: number;
}

export default {
    heartbeat(): Promise<AxiosResponse<void>> {
        return axiosApi.get('/heartbeat');
    },


    // words
    getAllRecentlyAddedWords(
        page: number,
        pageSize: number,
        lastLoadedWordId: number = -1,
        rollOut: boolean = false
    ): Promise<AxiosResponse<RecentlyAddedForeignWordsPageDto>> {
        return axiosApi.get(
            `/words?rollOut=${rollOut}&page=${page}&pageSize=${pageSize}&startsFromId=${lastLoadedWordId}`
        );
    },

    getAutocompleteSuggestions(term: string): Promise<AxiosResponse<string[]>> {
        return axiosApi.get('/words/autosuggestions?word=' + term);
    },

    addNewWord(body: AddNewWordRequestDto): Promise<AxiosResponse<void>> {
        return axiosApi.post('/words', body);
    },

    getByName(name: string): Promise<AxiosResponse<ForeignWordDetailDto>> {
        return axiosApi.get(`/words/${name}`);
    },

    async getTotalAddedWords(): Promise<number> {
        const response = await axiosApi.get(`/words/total`);
        return response.data.total;
    },

    getAllAddLaterWords(): Promise<AxiosResponse<string[]>> {
        return axiosApi.get('/words/later');
    },

    deleteWordFromAddLaterList(word: string): Promise<AxiosResponse<void>> {
        return axiosApi.delete('/words/later/' + word);
    },

    getWordsSummaryGraph(): Promise<AxiosResponse<DateCountDto[]>> {
        return axiosApi.get('/words/summary/graph');
    },


    // meanings
    linkContext(meaningId: number, body: WordMeaningContextDto): Promise<AxiosResponse<WordMeaningContextDto>> {
        return axiosApi.post('/meanings/' + meaningId + '/contexts', body);
    },

    linkCollocation(meaningId: number, body: WordMeaningCollocationDto): Promise<AxiosResponse<WordMeaningCollocationDto>> {
        return axiosApi.post('/meanings/' + meaningId + '/collocations', body);
    },

    editVariants(meaningId: number, russian: string | null, english: string | null): Promise<AxiosResponse<void>> {
        return axiosApi.patch('/meanings/' + meaningId + '/variants', { russian, english });
    },

    editImageUrl(meaningId: number, newImageUrl: string): Promise<AxiosResponse<void>> {
        return axiosApi.patch('/meanings/' + meaningId + '/image', { newImageUrl });
    },

    editCollocation(meaningId: number, collocationId: number, request: WordMeaningCollocationDto): Promise<AxiosResponse<WordMeaningCollocationDto>> {
        return axiosApi.put('/meanings/' + meaningId + '/collocations/' + collocationId, request);
    },

    removeCollocation(meaningId: number, collocationId: number): Promise<AxiosResponse<void>> {
        return axiosApi.delete('/meanings/' + meaningId + '/collocations/' + collocationId);
    },

    getMeaningsStatistics(): Promise<AxiosResponse<PartOfSpeechStatisticsDto[]>> {
        return axiosApi.get('/meanings/statistics');
    },


    // lists
    getAllLists(excludeSmart: boolean = false): Promise<AxiosResponse<VocabularyListDto[]>> {
        const smartParamString = excludeSmart ? '?excludeSmart=true' : '';
        return axiosApi.get('/lists' + smartParamString);
    },

    addNewList(name: string): Promise<AxiosResponse<VocabularyListDto>> {
        return axiosApi.post('/lists', { name });
    },

    getListById(id: number): Promise<AxiosResponse<VocabularyListDetailDto>> {
        return axiosApi.get(`/lists/${id}`);
    },

    deleteList(id: number): Promise<AxiosResponse<void>> {
        return axiosApi.delete(`/lists/${id}`);
    },


    // repetition
    async getTotalWordsToRepeatToday(): Promise<number> {
        const response = await axiosApi.get(`/repetition/today/total`);
        return response.data.total;
    },

    getPlannedRepetitionsSummaryGraph(): Promise<AxiosResponse<DateCountDto[]>> {
        return axiosApi.get('/repetition/summary/graph');
    },

    getWordsToRepeatToday(): Promise<AxiosResponse<WordMeaningDto[]>> {
        return axiosApi.get('/repetition/today');
    },

    scoreWordMeaning(meaningId: number, newScore: number): Promise<AxiosResponse<void>> {
        return axiosApi.put(`/repetition/meanings/${meaningId}?newScore=${newScore}`);
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
