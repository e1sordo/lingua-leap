<template>
    <div>
        <div class="container-sm">

            <div v-for="(word, index) in addLaterWords" :key="index" class="m-2 m-md-3 d-inline-flex btn-group"
                role="group">
                <button @click="$router.push('/add?word=' + word)" type="button" class="btn btn-success">
                    {{ word }}
                </button>
                <button @click="deleteWordFromAddLater(word)" type="button" class="btn btn-success">
                    <i class="bi bi-x-circle"></i>
                </button>
            </div>

            <div v-for="(dateStatistics, index) in datesStatistics" :key="index">
                <p class="mt-5">{{ convertDateToSinceString(dateStatistics.date) }} ({{ dateStatistics.totalWords }} слов)
                </p>
                <RecentlyAddedWords :data="wordsByDate(dateStatistics.date)" />
            </div>

        </div>
    </div>
</template>

<script lang="ts">
import api, { ForeignWordDto } from "@/api/backend-api";
import RecentlyAddedWords from '@/components/RecentlyAddedWords.vue';
import { partOfSpeechMeta } from '@/constants';
import { convertDateToSinceString } from '@/utils/convertDateToSinceString';
import { defineComponent } from 'vue';

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

export default defineComponent({
    name: 'FeedView',
    components: {
        RecentlyAddedWords
    },
    setup() {
        return { partOfSpeechMeta, convertDateToSinceString };
    },
    data() {
        return {
            latestWords: [] as ForeignWordDto[],
            addLaterWords: [] as string[]
        };
    },
    mounted() {
        this.fetchLatestWords();
        this.fetchWordsToAddLater();
    },
    computed: {
        datesStatistics(): DateStatistics[] {
            const datesStatistics = new Map<Date, DateStatistics>();
            this.latestWords.forEach(word => {
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
        }
    },
    methods: {
        async fetchLatestWords() {
            try {
                const response = await api.getAllRecentlyAddedWords();
                this.latestWords = response.data;
            } catch (error) {
                console.error(error);
            }
        },
        async fetchWordsToAddLater() {
            try {
                const response = await api.getAllAddLaterWords();
                this.addLaterWords = response.data;
            } catch (error) {
                console.error(error);
            }
        },
        deleteWordFromAddLater(word: string) {
            api.deleteWordFromAddLaterList(word).then(() => {
                this.addLaterWords = this.addLaterWords.filter(w => w !== word);
            });
        },
        wordsByDate(date: Date) {
            return this.latestWords
                .filter(word => word.addedDate === date);
        }
    }
});
</script>
