<template>
    <div>
        <div class="container-sm">

            <form @submit.prevent="submitForm" class="m-4">
                <div class="mb-3">
                    <div class="input-group input-group-lg">
                        <input v-model="newListName" id="listName" type="text" class="form-control"
                            placeholder="Enter name of a new list..." aria-label="Create new List"
                            aria-describedby="button-add-list">
                        <button class="btn btn-outline-secondary" type="submit" id="button-add-list">Create</button>
                    </div>
                </div>
            </form>

            <div v-for="wordsList in lists" :key="wordsList.id" class="m-2 m-md-3 d-inline-flex btn-group" role="group">
                <button @click="$router.push('/lists/' + wordsList.id)" type="button" class="btn"
                    :class="wordsList.smart ? 'text-bg-success' : 'text-bg-primary'">
                    <span class="fs-5 fw-normal py-2 px-3 badge">
                        <i v-if="wordsList.smart" class="bi bi-cpu pe-2"></i> {{ wordsList.name }}
                    </span>
                </button>
                <button v-if="!wordsList.smart" type="button" class="btn btn-primary" @click="deleteList(wordsList.id)">
                    <i class="bi bi-x-circle"></i>
                </button>
            </div>

        </div>
    </div>
</template>

<script setup lang="ts">
import api, { VocabularyListDto } from "@/api/backend-api";
import { onMounted, ref } from 'vue';

const lists = ref([] as VocabularyListDto[]);
const newListName = ref('');

const fetchLists = async () => {
    try {
        const response = await api.getAllLists();
        const sortedLists = response.data.sort((a, b) => {
            if (a.smart && !b.smart) {
                return -1;
            }
            return 0;
        });
        lists.value = sortedLists;
    } catch (error) {
        console.error(error);
    }
};

onMounted(() => {
    fetchLists();
});

const submitForm = () => {
    try {
        api.addNewList(newListName.value)
            .then((response) => {
                console.log("List was added");
                newListName.value = '';
                lists.value.push(response.data);
            });
    } catch (ex) {
        console.log("Error", ex);
    }
}

const deleteList = async (listId: number) => {
    try {
        await api.deleteList(listId);
        lists.value = lists.value.filter(list => list.id !== listId);
    } catch (ex) {
        console.log("Error", ex);
    }
}
</script>
