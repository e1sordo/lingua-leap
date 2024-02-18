<template>
    <form class="d-flex position-relative" role="search">
        <input class="form-control me-2" v-model="searchQuery" type="search" placeholder="Search words" aria-label="Search"
            autocomplete="off" @keydown.esc="showAutocomplete = false" @keydown.enter.prevent=""
            @click="showAutocomplete = false">

        <div v-if="showAutocomplete" class="autocomplete-dropdown">
            <ul class="list-group">
                <li v-for="(suggestion, index) in autocompleteSuggestions" :key="index" class="list-group-item">
                    <router-link :to="`/words/${suggestion}`">
                        {{ suggestion }}
                    </router-link>
                </li>
            </ul>
        </div>

        <button class="btn btn-success" type="button" @click="$router.push(`/words/${searchQuery}`)">
            Search
        </button>
    </form>
</template>

<script setup lang="ts">
import api from "@/api/backend-api";
import { ref, watch } from 'vue';

const searchQuery = ref('');
const showAutocomplete = ref(false);
const autocompleteSuggestions = ref([] as String[]);

type Timer = ReturnType<typeof setTimeout>
let timer: Timer | null = null;

watch(searchQuery, async (newValue: string) => {
    if (timer) {
        clearTimeout(timer);
    }

    timer = setTimeout(async () => {
        if (newValue.trim().length === 0) {
            showAutocomplete.value = false;
            return;
        }
        try {
            const response = await api.getAutocompleteSuggestions(newValue);
            autocompleteSuggestions.value = response.data;

            if (autocompleteSuggestions.value.length > 0) {
                showAutocomplete.value = true;
            }
        } catch (error) {
            console.error(error);
            showAutocomplete.value = false;
        }
    }, 500);
});
</script>

<style scoped>
.autocomplete-dropdown {
    text-align: left;
    position: absolute;
    top: 60px;
    z-index: 1;
    padding: 4px;
    overflow-y: auto;
}
</style>