<template>
    <div>
        <div class="container-sm">

            <ul class="nav justify-content-center mt-2">
                <li class="nav-item">
                    <router-link to="/study/table" class="nav-link active">Words Table</router-link>
                </li>
                <li class="nav-item">
                    <router-link to="/study/numbers" class="nav-link active">Numbers</router-link>
                </li>
                <li class="nav-item">
                    <router-link to="/study/abbreviations" class="nav-link active">Abbreviations</router-link>
                </li>
            </ul>

            <hr class="my-5" />

            <div class="row justify-content-center">
                <div class="col-lg-7g">
                    <WordsTable :data="todayWords" :refresh="shuffleRefreshingBit" />

                    <hr class="my-5" />
                </div>
            </div>

            <div class="row justify-content-center">
                <div class="col-lg-7 col-xl-6">
                    <div v-if="currentWord !== null">
                        <FlashCarousel :meaning="currentWord" :onSubmit="nextWord" />
                    </div>
                    <div v-else>
                        <p>No more words</p>
                    </div>
                </div>
            </div>

            <hr class="my-5" />

            <RecentlyAddedWords :data="todayWords" />

        </div>
    </div>
</template>

<script setup lang="ts">
import api, { WordMeaningDto } from "@/api/backend-api";
import FlashCarousel from '@/components/FlashCarousel.vue';
import WordsTable from '@/components/WordsTable.vue';
import RecentlyAddedWords from '@/components/RecentlyAddedWords.vue';
import { computed, onMounted, ref } from 'vue';

const todayWords = ref<WordMeaningDto[]>([]);
const shuffledTodayWords = ref<WordMeaningDto[]>([]);
const wordsCount = computed(() => Object.keys(todayWords.value).length);
const currentWordIndex = ref(0);
const shuffleRefreshingBit = ref(true);

const shuffleArray = (array: WordMeaningDto[]) => array
    .map((value) => ({ ...value, sort: Math.random() }))
    .sort((a, b) => a.sort - b.sort);

const currentWord = ref<WordMeaningDto | null>(null);
const updateCurrentWord = () => {
    if (currentWordIndex.value < wordsCount.value) {
        currentWord.value = shuffledTodayWords.value[currentWordIndex.value];
    } else {
        currentWord.value = null;
    }
};

onMounted(async () => {
    try {
        const response = await api.getWordsToRepeatToday();
        todayWords.value = response.data;
        shuffledTodayWords.value = shuffleArray(response.data);
        updateCurrentWord();
    } catch (error) {
        console.error('Error fetching words:', error);
    }
});

const nextWord = () => {
    currentWordIndex.value++;
    updateCurrentWord();
};
</script>
