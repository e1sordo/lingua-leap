<template>
    <div>
        <div class="container-md mb-5">

            <p>
                <a v-for="(service, id) in dictionaryLinks" :key="id" :href="service.href"
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
                        <div v-for="languageCode in translationLanguageCodes" :key="languageCode" class="col-sm-6">
                            <div class="input-group input-group-lg">
                                <span class="input-group-text">
                                    {{ languageLabel(languageCode, languages) }}
                                </span>
                                <input v-model="meaning.translations[languageCode]"
                                    :id="'translation-' + languageCode" type="text" class="form-control"
                                    :placeholder="languageCode">
                                <button class="btn btn-outline-secondary" type="button"
                                    :id="'button-addon-' + languageCode"
                                    @click="editTranslation(index)">
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
                                            <h6 v-for="entry in orderedTranslations(collocation, activeCourse)"
                                                :key="entry.code" class="card-subtitle mb-2 text-body-secondary">
                                                {{ languageLabel(entry.code, languages) }}: {{ entry.text }}
                                            </h6>
                                            <a href="#" class="card-link" data-bs-toggle="modal"
                                                :data-bs-target="'#collocationEditModal' + collocation.id">
                                                Edit
                                            </a>
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
                                                <div v-for="languageCode in translationLanguageCodes" :key="languageCode"
                                                    class="mb-3">
                                                    <label :for="'collocation-translation-' + languageCode"
                                                        class="col-form-label">
                                                        {{ languageLabel(languageCode, languages) }}:
                                                    </label>
                                                    <input v-model="collocation.translations[languageCode]" type="text"
                                                        class="form-control"
                                                        :id="'collocation-translation-' + languageCode">
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
                                                @click="editCollocation(index, colIndex)">
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
import { useCourses } from '@/composables/useCourses';
import { getLanguageModule } from '@/languages';
import { languageLabel, normalizeTranslations, orderedTranslations } from '@/utils/translations';
import AddNewCollocationForm from '@/components/AddNewCollocationForm.vue';
import AddNewContextForm from '@/components/AddNewContextForm.vue';
import ActionModalWindow from '@/components/ActionModalWindow.vue';
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const { activeCourse, activeCourseId, languages, loadLanguages } = useCourses();

const translationLanguageCodes = computed(() =>
    activeCourse.value?.translationLanguageCodes || []
);

const word = ref('');
const meanings = ref([] as WordMeaningDto[]);
const addedDate = ref(new Date());

const dictionaryLinks = computed(() => {
    if (!word.value || !activeCourse.value) {
        return [];
    }

    const languageModule = getLanguageModule(activeCourse.value.targetLanguageCode);
    const primaryTranslationLanguageCode = activeCourse.value.translationLanguageCodes[0] || '';

    return languageModule.dictionaries
        .map(service => ({
            name: service.name,
            color: service.color,
            href: service.link(word.value, primaryTranslationLanguageCode)
        }))
        .filter(service => service.href !== null) as Array<{ name: string; color: string; href: string }>;
});

const fetchDetails = async () => {
    if (activeCourseId.value === null) {
        return;
    }

    try {
        const response = await api.getByName(activeCourseId.value, route.params.word as string);
        word.value = response.data.word;
        meanings.value = response.data.meanings;
        addedDate.value = response.data.addedDate as Date;
        document.title = document.title + ` ${word.value}`;
    } catch (error) {
        console.error(error);
    }
};

onMounted(() => {
    loadLanguages();
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

const editTranslation = async (meaningIndex: number) => {
    if (activeCourseId.value === null) {
        return;
    }

    const meaning = meanings.value[meaningIndex];
    meaning.translations = normalizeTranslations(meaning.translations);

    try {
        api.editTranslations(activeCourseId.value, meaning.id, meaning.translations);
    } catch (error) {
        console.error(error);
    }
};

const editDefinition = async (meaningIndex: number) => {
    if (activeCourseId.value === null) {
        return;
    }

    const newDefinition = meanings.value[meaningIndex].definition;

    const meaningId = meanings.value[meaningIndex].id;
    try {
        api.editDefinition(activeCourseId.value, meaningId, newDefinition || '');
    } catch (error) {
        console.error(error);
    }
};

const editImageUrl = async (meaningIndex: number) => {
    if (activeCourseId.value === null) {
        return;
    }

    const newImageUrl = meanings.value[meaningIndex].imageUrl;

    const meaningId = meanings.value[meaningIndex].id;
    try {
        api.editImageUrl(activeCourseId.value, meaningId, newImageUrl || '');
    } catch (error) {
        console.error(error);
    }
};

const editCollocation = async (meaningIndex: number, collocationIndex: number) => {
    if (activeCourseId.value === null) {
        return;
    }

    const meaningId = meanings.value[meaningIndex].id;
    const collocation = meanings.value[meaningIndex].collocations[collocationIndex];
    collocation.translations = normalizeTranslations(collocation.translations);

    try {
        api.editCollocation(activeCourseId.value, meaningId, collocation.id, collocation)
            .then((resp) => {
                meanings.value[meaningIndex].collocations[collocationIndex].resolvedPattern = resp.data.resolvedPattern;
            });
    } catch (error) {
        console.error(error);
    }
};

const removeCollocation = async (meaningIndex: number, collocationIndex: number) => {
    if (activeCourseId.value === null) {
        return;
    }

    const meaningId = meanings.value[meaningIndex].id;
    const collocation = meanings.value[meaningIndex].collocations[collocationIndex];
    try {
        api.removeCollocation(activeCourseId.value, meaningId, collocation.id)
            .then(() => {
                meanings.value[meaningIndex].collocations.splice(collocationIndex, 1);
            });
    } catch (error) {
        console.error(error);
    }
};

const deleteWord = () => {
    if (activeCourseId.value === null) {
        return;
    }

    try {
        api.deleteWord(activeCourseId.value, word.value);
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
