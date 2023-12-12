<template>
  <div>
    <div class="container-sm">

      <div v-for="(dateStatistics, index) in datesStatistics" :key="index">
        <p class="mt-5">{{ convertDateToSinceString(dateStatistics.date) }} ({{ dateStatistics.totalWords }} слов)</p>
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
      latestWords: [] as ForeignWordDto[]
    };
  },
  mounted() {
    this.fetchLatestWords();
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
    wordsByDate(date: Date) {
      return this.latestWords
        .filter(word => word.addedDate === date);
    }
  }
});
</script>
