<template>
    <div>
        <button class="btn btn-secondary btn-sm" @click="showForm">Добавить коллокацию</button>

        <form @submit.prevent="submitForm" @hidden="resetForm" v-if="modalVisible">
            <div class="row mb-3 mt-2">
                <div class="col-sm-4">
                    <div class="form-floating">
                        <textarea v-model="pattern" id="collocation-original" type="text" class="form-control"
                            style="height: 100px" placeholder="Leave a sentence here" />
                        <label for="collocation-original">Паттерн</label>
                    </div>
                </div>
                <div v-for="languageCode in translationLanguageCodes" :key="languageCode" class="col-sm-4">
                    <div class="form-floating">
                        <textarea v-model="translations[languageCode]"
                            :id="'collocation-translate-' + languageCode" type="text" class="form-control"
                            style="height: 100px" :placeholder="languageCode" />
                        <label :for="'collocation-translate-' + languageCode">
                            {{ languageLabel(languageCode, languages) }} ({{ languageCode }})
                        </label>
                    </div>
                </div>
            </div>
            <button type="submit" class="btn btn-primary">Добавить</button>
        </form>
    </div>
</template>

<script lang="ts">
import api, { TranslationMap, WordMeaningCollocationDto } from '@/api/backend-api';
import { useCourses } from '@/composables/useCourses';
import { languageLabel, normalizeTranslations } from '@/utils/translations';
import { defineComponent, PropType } from 'vue';

export default defineComponent({
    props: {
        meaningId: {
            type: Number as PropType<number>,
            required: true,
        },
        onAddItem: {
            type: Function as PropType<(item: WordMeaningCollocationDto) => void>,
            required: true,
        },
    },
    data() {
        return {
            modalVisible: false,
            pattern: '',
            translations: {} as TranslationMap,
        };
    },
    setup() {
        const { activeCourseId, activeCourse, languages, loadLanguages } = useCourses();
        return { courseId: activeCourseId, activeCourse, languages, loadLanguages, languageLabel };
    },
    computed: {
        translationLanguageCodes(): string[] {
            return this.activeCourse?.translationLanguageCodes || [];
        }
    },
    mounted() {
        this.loadLanguages();
    },
    methods: {
        showForm() {
            this.modalVisible = true;
        },
        resetForm() {
            this.modalVisible = false;
            this.pattern = '';
            this.translations = {};
        },
        submitForm() {
            const newItem = {
                pattern: this.pattern,
                resolvedPattern: this.pattern,
                translations: normalizeTranslations(this.translations)
            } as WordMeaningCollocationDto;

            if (this.courseId === null) {
                return;
            }

            try {
                api.linkCollocation(this.courseId, this.meaningId, newItem)
                    .then((resp) => {
                        this.onAddItem(resp.data);
                        console.log("Collocation was linked");
                        this.resetForm();
                    });
            } catch (ex) {
                console.log("Error", ex);
            }
        }
    },
});
</script>
