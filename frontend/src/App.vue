<template>
    <div class="position-relative z-2">
        <template v-if="isWinter">
            <LightRope />
        </template>
        <nav class="navbar navbar-expand-lg" style="background-color: rgb(0, 93, 235)">
            <div class="container-fluid">
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarToggler"
                    aria-controls="navbarToggler" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarToggler">
                    <router-link to="/" class="navbar-brand">
                        <div spellcheck="false" class="brand fw-bolder">
                            Lingua Leap {{ totalWords }}
                        </div>
                    </router-link>
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                        <li class="nav-item">
                            <router-link to="/add" class="nav-link link-light">
                                {{ $t("navbar.add") }}
                            </router-link>
                        </li>
                        <li class="nav-item">
                            <router-link to="/study" class="nav-link link-light">
                                {{ $t("navbar.study") }} <span v-if="totalWordsToRepeatToday > 0"
                                    class="badge text-bg-danger position-relative" style="bottom: 8px;">
                                    {{ totalWordsToRepeatToday }}
                                </span>
                            </router-link>
                        </li>
                        <li class="nav-item">
                            <router-link to="/lists" class="nav-link link-light">
                                {{ $t("navbar.lists") }}
                            </router-link>
                        </li>
                        <li class="nav-item">
                            <router-link to="/settings" class="nav-link link-light">
                                {{ $t("navbar.settings") }}
                            </router-link>
                        </li>
                    </ul>
                    <word-search-form />
                </div>
            </div>
        </nav>
    </div>

    <div class="py-3" style="background-color: rgba(187, 187, 187, 0.12);">
        <div v-for="(meaningStatistics, index) in meaningsStatistics" :key="index" class="d-inline m-2 text-nowrap">
            <span data-bs-toggle="tooltip" :data-bs-title="partOfSpeechMeta[meaningStatistics.pos].label"
                data-bs-placement="bottom">
                {{ partOfSpeechMeta[meaningStatistics.pos].labelEsp }}:
            </span>
            <span class="badge text-bg-secondary pl-2">{{ meaningStatistics.count }}</span>
        </div>
    </div>

    <div style="height: 40px;"></div>

    <router-view :key="$route.fullPath" @updateTotalWords="updateTotalWords" />

    <div style="height: 100px;"></div>
</template>

<script setup lang="ts">
import api, { PartOfSpeechStatisticsDto } from "@/api/backend-api";
import WordSearchForm from '@/components/WordSearchForm.vue';
import LightRope from "./components/LightRope.vue";
import { partOfSpeechMeta } from '@/constants';
import { Tooltip } from 'bootstrap';
import { onMounted, provide, ref } from 'vue';

const totalWords = ref(0);
const totalWordsToRepeatToday = ref(0);
const meaningsStatistics = ref([] as PartOfSpeechStatisticsDto[]);
const isWinter = ref(false);

const fetchTotalAddedWordsNumber = async () => {
    try {
        totalWords.value = await api.getTotalAddedWords();
    } catch (error) {
        console.error(error);
    }
};

const fetchTotalWordsToRepeatToday = async () => {
    try {
        totalWordsToRepeatToday.value = await api.getTotalWordsToRepeatToday();
    } catch (error) {
        console.error(error);
    }
};

const fetchMeaningsStatistics = async () => {
    try {
        const response = await api.getMeaningsStatistics();
        meaningsStatistics.value = response.data.sort((a, b) => b.count - a.count);
    } catch (error) {
        console.error(error);
    }
};

const isWinterDay = () => {
    const date = new Date();
    const month = date.getMonth();
    const day = date.getDate();

    // Winter months: December (11), January (0), February (1)
    if (month === 11 || month === 0 || month === 1) {
        // Check if day falls within winter season
        if (month === 11 && day >= 10) { // December 10 or later
            return true;
        } else if (month === 1 && day <= 19) { // February 19 or earlier
            return true;
        } else {
            return true;
        }
    }

    return false;
}

onMounted(() => {
    fetchTotalAddedWordsNumber();
    fetchTotalWordsToRepeatToday();
    fetchMeaningsStatistics();
    isWinter.value = isWinterDay();

    new Tooltip(document.body, {
        selector: "[data-bs-toggle='tooltip']",
    });
});

const updateTotalWords = (updateFunction: (currentValue: number) => number) => {
    totalWords.value = updateFunction(totalWords.value);
    totalWordsToRepeatToday.value = updateFunction(totalWordsToRepeatToday.value);
};

const decreaseTotalWordsToRepeatToday = () => {
    totalWordsToRepeatToday.value = totalWordsToRepeatToday.value - 1;
};

provide('decreaseTotalWordsToRepeatToday', decreaseTotalWordsToRepeatToday);
</script>

<style>
#app {
    /* font-family: Avenir, Helvetica, Arial, sans-serif; */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
}

@media (prefers-color-scheme: light) {
    body {
        background-color: rgba(30, 32, 32, 0.03) !important;
    }
}

@media (prefers-color-scheme: dark) {
    body {
        background-color: rgb(1, 36, 49) !important;
    }
}

.brand {
    font-weight: 800;
    font-size: 32px;
    outline: none;
    background: linear-gradient(135deg, #fb0000 0%, #f8930f 25%, rgb(215, 208, 12) 50%, #f8930f 75%, #fb0000 100%);
    background-size: 400%;
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: animate 20s linear infinite;
}

@keyframes animate {
    to {
        background-position: 400%;
    }
}

nav {
    padding: 30px;
}

nav a {
    font-weight: bold;
    color: #777;
}

nav a.router-link-exact-active {
    color: #42b983;
}

.spoiler {
    filter: blur(4px);
    cursor: pointer;
}
</style>
