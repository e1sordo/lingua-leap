<template>
    <div>
        <div class="container-sm">

            <form @submit.prevent="submitForm" class="m-4">
                <div class="mb-3">
                    <label for="listName" class="form-label">List Name:</label>
                    <div class="input-group input-group-lg">
                        <input v-model="newListName" id="listName" type="text" class="form-control"
                            placeholder="Enter name..." aria-label="Create new List" aria-describedby="button-add-list">
                        <button class="btn btn-outline-secondary" type="submit" id="button-add-list">Create</button>
                    </div>
                </div>
            </form>

            <div v-for="wordsList in lists" :key="wordsList.id" class="m-2 m-md-3 d-inline-flex">
                <router-link :to="'/lists/' + wordsList.id">
                    <span class="fs-5 fw-normal py-2 px-3 badge" :class="wordsList.smart ? 'text-bg-success' : 'text-bg-primary'">
                        <i v-if="wordsList.smart" class="bi bi-cpu pe-2"></i> {{ wordsList.name }}
                    </span>
                </router-link>
            </div>

        </div>
    </div>
</template>

<script lang="ts">
import api, { VocabularyListDto } from "@/api/backend-api";
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'ListsView',
    data() {
        return {
            lists: [] as VocabularyListDto[],
            newListName: ''
        };
    },
    mounted() {
        this.fetchLists();
    },
    methods: {
        async fetchLists() {
            try {
                const response = await api.getAllLists();
                const sortedLists = response.data.sort((a, b) => {
                    if (a.smart && !b.smart) {
                        return -1;
                    }
                    return 0;
                });
                this.lists = sortedLists;
            } catch (error) {
                console.error(error);
            }
        },
        submitForm() {
            try {
                api.addNewList(this.newListName)
                    .then((response) => {
                        console.log("List was added");
                        this.newListName = '';
                        this.lists.push(response.data);
                    });
            } catch (ex) {
                console.log("Error", ex);
            }
        }
    }
});
</script>
