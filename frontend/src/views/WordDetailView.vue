<template>
    <div>
        <div class="container-md mb-5">

            <p>
                <a :href="`https://www.spanishdict.com/translate/${word}`" class="btn btn-outline-primary btn-lg m-2"
                    target="_blank">
                    Span¡shD!ctionary
                </a>
                <a :href="`https://youglish.com/pronounce/${word}/spanish`" class="btn btn-outline-success btn-lg m-2"
                    target="_blank">
                    YouGlish
                </a>
            </p>

            <div class="my-4">
                <h1 class="display-1 fw-semibold">{{ word }}</h1>
            </div>

            <div v-for="(meaning, index) in meanings" :key="index" class="card text-start mb-4">
                <div class="card-body">

                    <div class="d-flex justify-content-start align-items-center mb-4">
                        <h5 class="card-title mb-0 me-3">Значение слова #{{ index + 1 }}</h5>
                        <span class="me-3">{{ meaning.pos }}</span>
                        <span v-if="meaning.pos === 'NOUN'">{{ meaning.gender }}</span>
                    </div>

                    <div class="row g-3 py-3">
                        <div class="col-sm-6">
                            <div class="input-group input-group-lg">
                                <span class="input-group-text">🇷🇺</span>
                                <input v-model="meaning.russianVariant" id="russianVariant" type="text" required
                                    class="form-control" placeholder="Перевод на русский">
                                <button class="btn btn-outline-secondary" type="button" id="button-addon-rus"
                                    @click="editVariants(index, meaning.russianVariant, null)">
                                    <i class="bi bi-pencil-square"></i>
                                </button>
                            </div>
                        </div>
                        <div class="col-sm-6">
                            <div class="input-group input-group-lg">
                                <span class="input-group-text">🇬🇧</span>
                                <input v-model="meaning.englishVariant" id="englishVariant" type="text" required
                                    class="form-control" placeholder="Translation into English">
                                <button class="btn btn-outline-secondary" type="button" id="button-addon-eng"
                                    @click="editVariants(index, null, meaning.englishVariant)">
                                    <i class="bi bi-pencil-square"></i>
                                </button>
                            </div>
                        </div>
                    </div>

                    <div v-if="meaning.definition" class="my-3">
                        {{ meaning.definition }}
                    </div>

                    <div class="row g-3 my-3">
                        <div :class="meaning.imageUrl ? 'col-sm-8' : 'col-sm-11'">
                            <h5 class="card-title pb-2">📖 Примеры употребления</h5>

                            <div class="mx-lg-2">
                                <figure v-for="context in meaning.contexts" :key="context.id">
                                    <blockquote class="blockquote">
                                        <p>{{ context.sentence }}</p>
                                    </blockquote>
                                    <figcaption class="blockquote-footer">
                                        {{ context.translation }}
                                    </figcaption>
                                </figure>

                                <AddNewContextForm :meaningId="meaning.id"
                                    :onAddItem="(item) => addContextLocally(index, item)" />
                            </div>
                        </div>

                        <div v-if="meaning.imageUrl" class="col-md-4 col-sm-9">
                            <a href="#" data-bs-toggle="modal" :data-bs-target="'#imageUrlEditModal' + meaning.id">
                                <img :src="meaning.imageUrl" alt="Image" class="img-fluid w-100 img-thumbnail" />
                            </a>
                        </div>
                        <div v-else class="col-sm-1 text-end">
                            <a href="#" data-bs-toggle="modal" :data-bs-target="'#imageUrlEditModal' + meaning.id">🏞️</a>
                        </div>

                        <div class="modal fade" :id="'imageUrlEditModal' + meaning.id" tabindex="-1"
                            aria-labelledby="imageUrlEditModalLabel" aria-hidden="true">
                            <div class="modal-dialog">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h1 class="modal-title fs-5" id="imageUrlEditModalLabel">Change imageUrl</h1>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                                            aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <form>
                                            <div class="mb-3">
                                                <label for="image-url-text" class="col-form-label">Image URL:</label>
                                                <input v-model="meaning.imageUrl" type="text" class="form-control"
                                                    id="image-url-text">
                                            </div>
                                        </form>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                                            Close
                                        </button>
                                        <button type="button" class="btn btn-primary" data-bs-dismiss="modal"
                                            @click="editImageUrl(index, meaning.imageUrl)">
                                            Change
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>


            <div class="row g-3 my-4">
                <div class="col-auto">
                    Дата добавления: {{ addedDate }}
                </div>
            </div>

        </div>
    </div>
</template>

<script setup lang="ts">
import api, { WordMeaningContextDto, WordMeaningDto } from "@/api/backend-api";
import AddNewContextForm from '@/components/AddNewContextForm.vue';
import { onMounted, ref } from "vue";
import { useRoute } from 'vue-router';

const word = ref('');
const meanings = ref([] as WordMeaningDto[]);
const addedDate = ref(new Date());

const fetchDetails = async () => {
    try {
        const router = useRoute();
        const response = await api.getByName(router.params.word as string);
        word.value = response.data.word;
        meanings.value = response.data.meanings;
        addedDate.value = response.data.addedDate;
        document.title = document.title + ` ${word.value}`;
    } catch (error) {
        console.error(error);
    }
};

onMounted(() => {
    fetchDetails();
});

const addContextLocally = async (meaningIndex: number, newContext: WordMeaningContextDto) => {
    console.log(meaningIndex, newContext);
    meanings.value[meaningIndex].contexts.push(newContext);
};

const editVariants = async (meaningIndex: number, russianVariant: string | null, englishVariant: string | null) => {
    if (russianVariant) {
        meanings.value[meaningIndex].russianVariant = russianVariant;
    } else if (englishVariant) {
        meanings.value[meaningIndex].englishVariant = englishVariant;
    }

    const meaningId = meanings.value[meaningIndex].id;
    try {
        api.editVariants(meaningId, russianVariant, englishVariant);
    } catch (error) {
        console.error(error);
    }
};

const editImageUrl = async (meaningIndex: number, newImageUrl: string) => {
    meanings.value[meaningIndex].imageUrl = newImageUrl;

    const meaningId = meanings.value[meaningIndex].id;
    try {
        api.editImageUrl(meaningId, newImageUrl);
    } catch (error) {
        console.error(error);
    }
};
</script>
