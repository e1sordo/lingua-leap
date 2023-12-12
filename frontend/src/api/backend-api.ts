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
    contexts: WordMeaningContextDto[];
    lists: VocabularyListDto[];
}

export interface WordMeaningContextDto {
    id: number;
    sentence: string;
    translation: string;
}

export interface VocabularyListDto {
    id: number;
    name: string;
}

export interface VocabularyListDetailDto {
    id: number;
    name: string;
    words: WordMeaningDto[];
}

export default {
    heartbeat(): Promise<AxiosResponse<void>> {
        return axiosApi.get('/heartbeat');
    },


    // words
    getAllRecentlyAddedWords(rollOut: boolean = false): Promise<AxiosResponse<ForeignWordDto[]>> {
        return axiosApi.get(`/words?rollOut=${rollOut}`);
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


    // meanings
    linkContext(meaningId: number, body: WordMeaningContextDto): Promise<AxiosResponse<WordMeaningContextDto>> {
        return axiosApi.post('/meanings/' + meaningId, body);
    },

    getMeaningsStatistics(): Promise<AxiosResponse<PartOfSpeechStatisticsDto[]>> {
        return axiosApi.get('/meanings/statistics');
    },


    // lists
    getAllLists(): Promise<AxiosResponse<VocabularyListDto[]>> {
        return axiosApi.get('/lists');
    },

    addNewList(name: string): Promise<AxiosResponse<VocabularyListDto>> {
        return axiosApi.post('/lists', { name });
    },

    getListById(id: number): Promise<AxiosResponse<VocabularyListDetailDto>> {
        return axiosApi.get(`/lists/${id}`);
    },


    // repetition
    async getTotalWordsToRepeatToday(): Promise<number> {
        const response = await axiosApi.get(`/repetition/today/total`);
        return response.data.total;
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
