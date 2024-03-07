<template>
    <div>
        <div class="container-md mb-5">

            <p>
                <a v-for="(service, id) in dictionaryServices" :key="id" :href="service.link(word!!)"
                    class="btn btn-lg m-2" :class="`btn-outline-${service.color}`" target="_blank">
                    {{ service.name }}
                </a>
            </p>

            <div class="my-4">
                <h1 class="display-1 fw-semibold">{{ word }}</h1>
            </div>

            <div v-for="(meaning, index) in meanings" :key="index" class="card text-start my-4">
                <div class="card-body">

                    <div class="d-flex justify-content-start align-items-center mb-4">
                        <h5 class="card-title mb-0 me-3">Значение слова #{{ index + 1 }}</h5>
                        <span class="me-1" v-if="meaning.pos === 'NOUN'">{{ meaning.gender }}</span>
                        <span>{{ meaning.pos }}</span>
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


                    <div class="my-4 mb-5">
                        <h5 class="card-title pb-2">💬 Описание</h5>

                        <div class="ps-2">
                            {{ meaning.definition }}
                            <a href="#" class="card-link text-reset text-decoration-none" data-bs-toggle="modal"
                                :data-bs-target="'#definitionEditModal' + index">
                                <span v-if="!meaning.definition" class="pe-2">Добавить описание</span>
                                <i class="bi bi-pencil-fill"></i>
                            </a>

                            <ActionModalWindow :id="'definitionEditModal' + index" title="Change definition">
                                <div class="modal-body">
                                    <form>
                                        <div class="mb-3">
                                            <textarea v-model="meaning.definition" type="text" class="form-control"
                                                id="definition-text" style="height: 250px;"></textarea>
                                        </div>
                                    </form>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                                        Close
                                    </button>
                                    <button type="button" class="btn btn-primary" data-bs-dismiss="modal"
                                        @click="editDefinition(index)">
                                        Change
                                    </button>
                                </div>
                            </ActionModalWindow>
                        </div>
                    </div>


                    <div class="my-4">
                        <h5 class="card-title pb-2">🥇 Популярные коллокации</h5>

                        <div class="mx-lg-2">
                            <div class="row g-3 mb-3">
                                <div class="col-md-6 col-lg-4 col-xl-3"
                                    v-for="(collocation, colIndex) in meaning.collocations" :key="colIndex">
                                    <div class="card mb-1">
                                        <div class="card-body">
                                            <h5 class="card-title pb-2" v-html="collocation.resolvedPattern" />
                                            <h6 class="card-subtitle mb-2 text-body-secondary">
                                                🇷🇺 {{ collocation.translationRussian }}
                                            </h6>
                                            <h6 class="card-subtitle mb-2 text-body-secondary">
                                                🇺🇸 {{ collocation.translationEnglish }}
                                            </h6>
                                            <a href="#" class="card-link" data-bs-toggle="modal"
                                                :data-bs-target="'#collocationEditModal' + collocation.id">
                                                Edit
                                            </a>
                                            <!-- <a href="#" class="card-link">Another link</a> -->
                                        </div>
                                    </div>

                                    <ActionModalWindow :id="'collocationEditModal' + collocation.id"
                                        title="Change collocation">
                                        <div class="modal-body">
                                            <form>
                                                <div class="mb-3">
                                                    <label for="collocation-pattern-text" class="col-form-label">
                                                        Pattern:
                                                    </label>
                                                    <input v-model="collocation.pattern" type="text"
                                                        class="form-control" id="collocation-pattern-text">
                                                </div>
                                                <div class="mb-3">
                                                    <label for="collocation-translation-rus-text"
                                                        class="col-form-label">
                                                        🇷🇺:
                                                    </label>
                                                    <input v-model="collocation.translationRussian" type="text"
                                                        class="form-control" id="collocation-translation-rus-text">
                                                </div>
                                                <div class="mb-3">
                                                    <label for="collocation-translation-eng-text"
                                                        class="col-form-label">
                                                        🇺🇸:
                                                    </label>
                                                    <input v-model="collocation.translationEnglish" type="text"
                                                        class="form-control" id="collocation-translation-eng-text">
                                                </div>
                                            </form>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                                                Close
                                            </button>
                                            <button type="button" class="btn btn-danger" data-bs-dismiss="modal"
                                                @click="removeCollocation(index, colIndex)">
                                                Delete
                                            </button>
                                            <button type="button" class="btn btn-primary" data-bs-dismiss="modal"
                                                @click="editCollocation(index, colIndex, collocation.pattern, collocation.translationRussian, collocation.translationEnglish)">
                                                Change
                                            </button>
                                        </div>
                                    </ActionModalWindow>
                                </div>
                            </div>

                            <AddNewCollocationForm :meaningId="meaning.id"
                                :onAddItem="(item) => addCollocationLocally(index, item)" />
                        </div>
                    </div>


                    <div class="row g-3 my-4">
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
                            <a href="#" data-bs-toggle="modal"
                                :data-bs-target="'#imageUrlEditModal' + meaning.id">🏞️</a>
                        </div>

                        <ActionModalWindow :id="'imageUrlEditModal' + meaning.id" title="Change imageUrl">
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
                                    @click="editImageUrl(index)">
                                    Change
                                </button>
                            </div>
                        </ActionModalWindow>

                    </div>
                </div>
            </div>


            <div class="row g-3 my-4">
                <div class="col-auto">
                    Дата добавления: {{ addedDate }}
                </div>
                <div class="col-auto">
                    <button type="button" class="btn btn-sm btn-danger" data-bs-toggle="modal"
                        data-bs-target="#deleteWordConfirmationModal">
                        Delete
                    </button>

                    <div class="modal fade" id="deleteWordConfirmationModal" tabindex="-1"
                        aria-labelledby="deleteWordConfirmationModalLabel" aria-hidden="true">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h1 class="modal-title fs-5" id="deleteWordConfirmationModalLabel">
                                        Delete word <strong>{{ word }}</strong>
                                    </h1>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"
                                        aria-label="Close"></button>
                                </div>
                                <div class="modal-body">
                                    <p>Are you sure you want to delete this word?</p>
                                    <p>This is unrecoverable action.</p>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                                        Close
                                    </button>
                                    <button type="button" class="btn btn-danger" data-bs-dismiss="modal"
                                        @click="deleteWord">
                                        Delete
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </div>
</template>

<script setup lang="ts">
import api, { WordMeaningCollocationDto, WordMeaningContextDto, WordMeaningDto } from "@/api/backend-api";
import { dictionaryServices } from '@/constants';
import AddNewCollocationForm from '@/components/AddNewCollocationForm.vue';
import AddNewContextForm from '@/components/AddNewContextForm.vue';
import ActionModalWindow from '@/components/ActionModalWindow.vue';
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const word = ref('');
const meanings = ref([] as WordMeaningDto[]);
const addedDate = ref(new Date());

const fetchDetails = async () => {
    try {
        const response = await api.getByName(route.params.word as string);
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

const addCollocationLocally = async (meaningIndex: number, newCollocation: WordMeaningCollocationDto) => {
    console.log(meaningIndex, newCollocation);
    meanings.value[meaningIndex].collocations.push(newCollocation);
};

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

const editDefinition = async (meaningIndex: number) => {
    const newDefinition = meanings.value[meaningIndex].definition;

    const meaningId = meanings.value[meaningIndex].id;
    try {
        api.editDefinition(meaningId, newDefinition || '');
    } catch (error) {
        console.error(error);
    }
};

const editImageUrl = async (meaningIndex: number) => {
    const newImageUrl = meanings.value[meaningIndex].imageUrl;

    const meaningId = meanings.value[meaningIndex].id;
    try {
        api.editImageUrl(meaningId, newImageUrl || '');
    } catch (error) {
        console.error(error);
    }
};

const editCollocation = async (meaningIndex: number, collocationIndex: number, newPattern: string, newRusTranslation: string, newEngTranslation: string) => {
    meanings.value[meaningIndex].collocations[collocationIndex].pattern = newPattern;
    meanings.value[meaningIndex].collocations[collocationIndex].translationRussian = newRusTranslation;
    meanings.value[meaningIndex].collocations[collocationIndex].translationEnglish = newEngTranslation;

    const meaningId = meanings.value[meaningIndex].id;
    const collocation = meanings.value[meaningIndex].collocations[collocationIndex];
    try {
        api.editCollocation(meaningId, collocation.id, collocation)
            .then((resp) => {
                meanings.value[meaningIndex].collocations[collocationIndex].resolvedPattern = resp.data.resolvedPattern;
            });
    } catch (error) {
        console.error(error);
    }
};

const removeCollocation = async (meaningIndex: number, collocationIndex: number) => {
    const meaningId = meanings.value[meaningIndex].id;
    const collocation = meanings.value[meaningIndex].collocations[collocationIndex];
    try {
        api.removeCollocation(meaningId, collocation.id)
            .then(() => {
                meanings.value[meaningIndex].collocations.splice(collocationIndex, 1);
            });
    } catch (error) {
        console.error(error);
    }
};

const deleteWord = () => {
    try {
        api.deleteWord(word.value);
        router.push('/');
    } catch (ex) {
        console.log("Error", ex);
    }
}
</script>

<style scoped>
.main-word {
    font-weight: bold;
}
</style>