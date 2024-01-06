<template>
    <div>
        <div class="z-1 position-fixed top-0 start-0 w-100">
            <div class="progress" role="progressbar" style="border-radius: 0;">
                <div class="progress-bar progress-bar-striped progress-bar-animated"
                    :style="{ width: progressPercentage + '%' }"></div>
            </div>
        </div>

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

            <calendar-heatmap allowFutureDays :end-date="datePlusYear" :values="summaryGraph"
                :max="50" :round="3" no-data-text="No words for this day" tooltip-unit="words" :darkMode="prefersDarkScheme" />

            <hr class="my-5" />

            <div class="row justify-content-center">
                <div class="col-lg-7">
                    <div class="words-table-content" :style="{ maxHeight: wordsTableMaxHeight + 'px' }">
                        <WordsTable :data="todayWords" :refresh="shuffleRefreshingBit" />
                    </div>
                    <button class="btn btn-primary mt-2" @click="toggleSpoiler">
                        {{ isSpoilerVisible ? 'Скрыть' : 'Показать' }} всю таблицу
                    </button>

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
import api, { WordMeaningDto, DateCountDto } from "@/api/backend-api";
import FlashCarousel from '@/components/FlashCarousel.vue';
import CalendarHeatmap from '@/components/heatmap/CalendarHeatmap.vue';
import WordsTable from '@/components/WordsTable.vue';
import RecentlyAddedWords from '@/components/RecentlyAddedWords.vue';
import { computed, onMounted, ref } from 'vue';

const prefersDarkScheme = ref(window.matchMedia("(prefers-color-scheme: dark)").matches);

const summaryGraph = ref<DateCountDto[]>([]);
const todayWords = ref<WordMeaningDto[]>([]);
const shuffledTodayWords = ref<WordMeaningDto[]>([]);
const wordsCount = computed(() => Object.keys(todayWords.value).length);
const currentWordIndex = ref(0);
const progressPercentage = ref(0);
const shuffleRefreshingBit = ref(true);

const isSpoilerVisible = ref(false);
const wordsTableMaxHeight = ref(500);

const todayDate = new Date();
const datePlusYear = new Date(todayDate.getFullYear() + 1, todayDate.getMonth(), todayDate.getDate() - 1);

const toggleSpoiler = () => {
    isSpoilerVisible.value = !isSpoilerVisible.value;
    wordsTableMaxHeight.value = isSpoilerVisible.value ? 10000 : 500;
};

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
        const summaryResponse = await api.getPlannedRepetitionsSummaryGraph();
        summaryGraph.value = summaryResponse.data;

        const wordsResponse = await api.getWordsToRepeatToday();
        todayWords.value = wordsResponse.data;
        shuffledTodayWords.value = shuffleArray(wordsResponse.data);
        updateCurrentWord();
    } catch (error) {
        console.error('Error fetching words:', error);
    }
});

const nextWord = () => {
    currentWordIndex.value++;
    updateCurrentWord();
    progressPercentage.value = Math.floor((currentWordIndex.value / wordsCount.value) * 100);
};
</script>

<style scoped>
.words-table-content {
    overflow: hidden;
    transition: max-height 0.3s ease-out;
}
</style>
