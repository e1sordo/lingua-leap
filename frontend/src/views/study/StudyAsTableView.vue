<template>
  <div>
    <div class="container-sm">

      <div class="row justify-content-center">
        <div class="col-lg-7">
          <WordsTable :data="latestWords" :refresh="shuffleRefreshingBit" />
          <button class="btn btn-sm btn-outline-primary" @click="toggleShuffleBit()">Shuffle</button>
        </div>
      </div>

    </div>
  </div>
</template>

<script lang="ts">
import api, { ForeignWordDto } from "@/api/backend-api";
import WordsTable from '@/components/WordsTable.vue';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'StudyAsTableView',
  components: {
    WordsTable
  },
  data() {
    return {
      shuffleRefreshingBit: true,
      latestWords: [] as ForeignWordDto[]
    };
  },
  mounted() {
    this.fetchLatestWords();
  },
  methods: {
    async fetchLatestWords() {
      try {
        const response = await api.getAllRecentlyAddedWords(true);
        this.latestWords = response.data;
      } catch (error) {
        console.error(error);
      }
    },
    toggleShuffleBit() {
      this.shuffleRefreshingBit = !this.shuffleRefreshingBit;
    }
  }
});
</script>
