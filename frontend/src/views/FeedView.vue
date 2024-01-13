<template>
    <div>
        <div class="container-sm" ref="scrollComponent">

            <calendar-heatmap :end-date="todayDate" :values="summaryGraph" :max="21" :round="3"
                no-data-text="No words for this day" tooltip-unit="words" :darkMode="prefersDarkScheme" />

            <hr class="my-4" />

            <div v-if="addLaterWords.length > 0" class="text-center">
                <h5>Total words to add: <strong>{{ addLaterWords.length }}</strong></h5>
                <div v-for="(word, index) in addLaterWords" :key="index" class="m-2 d-inline-flex btn-group" role="group">
                    <button @click="$router.push('/add?word=' + word)" type="button" class="btn btn-outline-success btn-sm">
                        <strong>{{ word }}</strong>
                    </button>
                    <button @click="deleteWordFromAddLater(word)" type="button" class="btn btn-outline-success btn-sm">
                        <i class="bi bi-x-circle"></i>
                    </button>
                </div>
                <hr />
            </div>

            <div v-for="(dateStatistics, index) in datesStatistics" :key="index">
                <p class="mt-5">
                    {{ convertDateToSinceString(dateStatistics.date) }}
                    (<strong>{{ dateStatistics.totalWords }}</strong> слов)
                </p>
                <RecentlyAddedWords :data="wordsByDate(dateStatistics.date)" />
            </div>

        </div>
    </div>
</template>

<script setup lang="ts">
import api, { DateCountDto, ForeignWordDto } from "@/api/backend-api";
import RecentlyAddedWords from '@/components/RecentlyAddedWords.vue';
import CalendarHeatmap from '@/components/heatmap/CalendarHeatmap.vue';
import { convertDateToSinceString } from '@/utils/convertDateToSinceString';
import { computed, onMounted, onUnmounted, ref } from 'vue';

class DateStatistics {
    date: Date;
    totalWords: number;

    constructor(date: Date) {
        this.date = date;
        this.totalWords = 1;
    }
    incCounter() {
        this.totalWords += 1;
    }
}

const prefersDarkScheme = ref(window.matchMedia("(prefers-color-scheme: dark)").matches);

const summaryGraph = ref<DateCountDto[]>([]);
const todayDate = new Date();

const stopLoading = ref(false);
const isLoading = ref(false);
const currentPage = ref(0);
const batchSize = ref(100);
const scrollComponent = ref<HTMLDivElement>();

const latestWords = ref(new Set<ForeignWordDto>());
const lastLoadedWordId = ref(-1);
const addLaterWords = ref([] as string[]);

const fetchLatestWords = async () => {
    if (isLoading.value) {
        return;
    }

    isLoading.value = true;
    try {
        const page = currentPage.value;
        const response = await api.getAllRecentlyAddedWords(page, batchSize.value, lastLoadedWordId.value);
        const responseDto = response.data;
        const newList = responseDto.data;
        if (newList.length === 0) {
            stopLoading.value = true;
            window.removeEventListener("scroll", handleScroll);
        } else {
            lastLoadedWordId.value = newList[newList.length - 1].id;
            newList.forEach(item => latestWords.value.add(item));

            if (responseDto.isLast) {
                stopLoading.value = true;
                window.removeEventListener("scroll", handleScroll);
            } else {
                currentPage.value++;
            }
        }
    } catch (error) {
        console.error(error);
    } finally {
        isLoading.value = false;
    }
};

const fetchSummaryGraph = async () => {
    try {
        const response = await api.getWordsSummaryGraph();
        summaryGraph.value = response.data;
    } catch (error) {
        console.error(error);
    }
};

const fetchWordsToAddLater = async () => {
    try {
        const response = await api.getAllAddLaterWords();
        addLaterWords.value = response.data;
    } catch (error) {
        console.error(error);
    }
};

const handleScroll = () => {
    let element = scrollComponent.value;
    if (element && element.getBoundingClientRect().bottom < window.innerHeight) {
        if (!stopLoading.value) {
            fetchLatestWords();
        }
    }
};

onMounted(() => {
    window.addEventListener("scroll", handleScroll);
    fetchSummaryGraph();
    fetchLatestWords();
    fetchWordsToAddLater();
});

onUnmounted(() => {
    window.removeEventListener("scroll", handleScroll);
});

const datesStatistics = computed(() => {
    const datesStatistics = new Map<Date, DateStatistics>();
    latestWords.value.forEach(word => {
        const addedDate = word.addedDate!!;
        if (datesStatistics.has(addedDate)) {
            const statistics = datesStatistics.get(addedDate)!!;
            statistics.incCounter();
            datesStatistics.set(addedDate, statistics);
        } else {
            datesStatistics.set(addedDate, new DateStatistics(addedDate));
        }
    });
    return Array.from(datesStatistics.values());
});

const deleteWordFromAddLater = (word: string) => {
    api.deleteWordFromAddLaterList(word).then(() => {
        addLaterWords.value = addLaterWords.value.filter(w => w !== word);
    });
};

const wordsByDate = (date: Date) => {
    return [...latestWords.value].filter(word => word.addedDate === date);
};
</script>
