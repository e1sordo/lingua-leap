<template>
    <ActionModalWindow id="courseCreateModal" :title="$t('course.createTitle')">
        <div class="modal-body">
            <form id="courseCreateForm" @submit.prevent="submitForm">
                <div class="mb-3">
                    <label for="courseTargetLanguage" class="form-label">
                        {{ $t("course.targetLanguage") }}
                    </label>
                    <select v-model="targetLanguageCode" id="courseTargetLanguage" class="form-select" required>
                        <option value="" disabled>{{ $t("course.selectLanguage") }}</option>
                        <option v-for="language in languages" :key="language.code" :value="language.code">
                            {{ language.name }}
                        </option>
                    </select>
                </div>

                <div class="mb-3">
                    <label for="coursePrimaryTranslationLanguage" class="form-label">
                        {{ $t("course.primaryTranslationLanguage") }}
                    </label>
                    <select v-model="primaryTranslationLanguageCode" id="coursePrimaryTranslationLanguage"
                        class="form-select" required>
                        <option value="" disabled>{{ $t("course.selectLanguage") }}</option>
                        <option v-for="language in translationLanguageOptions" :key="language.code"
                            :value="language.code">
                            {{ language.name }}
                        </option>
                    </select>
                </div>

                <div class="mb-3">
                    <label for="courseSecondaryTranslationLanguage" class="form-label">
                        {{ $t("course.secondaryTranslationLanguage") }}
                    </label>
                    <select v-model="secondaryTranslationLanguageCode" id="courseSecondaryTranslationLanguage"
                        class="form-select">
                        <option value="">{{ $t("course.none") }}</option>
                        <option v-for="language in secondaryLanguageOptions" :key="language.code" :value="language.code">
                            {{ language.name }}
                        </option>
                    </select>
                </div>

                <div v-if="errorMessage" class="alert alert-danger py-2 mb-0">
                    {{ errorMessage }}
                </div>
            </form>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                {{ $t("course.cancel") }}
            </button>
            <button type="submit" form="courseCreateForm" class="btn btn-primary" :disabled="submitting">
                {{ $t("course.create") }}
            </button>
        </div>
    </ActionModalWindow>
</template>

<script setup lang="ts">
import ActionModalWindow from '@/components/ActionModalWindow.vue';
import { useCourses } from '@/composables/useCourses';
import { Modal } from 'bootstrap';
import { computed, onMounted, ref } from 'vue';

const { createCourse, loadLanguages, languages } = useCourses();
const targetLanguageCode = ref('');
const primaryTranslationLanguageCode = ref('');
const secondaryTranslationLanguageCode = ref('');
const errorMessage = ref('');
const submitting = ref(false);

const translationLanguageOptions = computed(() =>
    languages.value.filter(language => language.code !== targetLanguageCode.value)
);

const secondaryLanguageOptions = computed(() =>
    translationLanguageOptions.value.filter(
        language => language.code !== primaryTranslationLanguageCode.value
    )
);

onMounted(async () => {
    await loadLanguages();
});

const submitForm = async () => {
    errorMessage.value = '';
    submitting.value = true;

    const translationLanguageCodes = [primaryTranslationLanguageCode.value];
    if (secondaryTranslationLanguageCode.value) {
        translationLanguageCodes.push(secondaryTranslationLanguageCode.value);
    }

    try {
        await createCourse({
            targetLanguageCode: targetLanguageCode.value,
            translationLanguageCodes
        });

        targetLanguageCode.value = '';
        primaryTranslationLanguageCode.value = '';
        secondaryTranslationLanguageCode.value = '';

        Modal.getOrCreateInstance(document.getElementById('courseCreateModal')!).hide();
    } catch (error: any) {
        console.error(error);
        errorMessage.value = error?.response?.data?.message || String(error?.message || error);
    } finally {
        submitting.value = false;
    }
};
</script>
