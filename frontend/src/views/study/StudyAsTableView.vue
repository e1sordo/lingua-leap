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

<script setup lang="ts">
import api, { ForeignWordDto } from "@/api/backend-api";
import WordsTable from '@/components/WordsTable.vue';
import { onMounted, ref } from 'vue';

const shuffleRefreshingBit = ref(true);
const latestWords = ref([] as ForeignWordDto[]);

const fetchLatestWords = async () => {
    try {
        const response = await api.getAllRecentlyAddedWords(0, 100, -1, true);
        latestWords.value = response.data.data;
    } catch (error) {
        console.error(error);
    }
};

onMounted(() => {
    fetchLatestWords();
});

const toggleShuffleBit = () => {
    shuffleRefreshingBit.value = !shuffleRefreshingBit.value;
};
</script>
