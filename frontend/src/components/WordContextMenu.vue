<template>
    <div class="dropdown">
        <div aria-expanded="false" data-bs-toggle="dropdown" type="button">
            <slot></slot>
        </div>

        <ul class="dropdown-menu">
            <li>
                <router-link :to="`/words/${word}`" class="dropdown-item">
                    Detail
                </router-link>
            </li>

            <li v-if="customActions?.length || 0 > 0">
                <hr class="dropdown-divider" />
            </li>
            <li v-if="customActions?.length || 0 > 0">
                <h6 class="dropdown-header">Custom actions</h6>
            </li>
            <li v-for="(action, index) in customActions" :key="index">
                <a class="dropdown-item" :href="action.link" target="_blank">
                    {{ action.name }}
                </a>
            </li>

            <template v-if="availableDictionaries.length > 0">
                <li>
                    <hr class="dropdown-divider" />
                </li>
                <li>
                    <h6 class="dropdown-header">Tools</h6>
                </li>
                <li v-for="(service, id) in availableDictionaries" :key="id">
                    <a class="dropdown-item" :href="service.link" target="_blank">
                        {{ service.name }}
                        <i class="bi bi-box-arrow-up-right text-primary"></i>
                    </a>
                </li>
            </template>
        </ul>
    </div>
</template>

<script setup lang="ts">
import { useCourses } from '@/composables/useCourses';
import { getLanguageModule } from '@/languages';
import { computed, PropType } from 'vue';

interface CustomAction {
    name: string;
    link: string;
}

const props = defineProps({
    word: String,
    customActions: Array as PropType<CustomAction[]>,
})

const { activeCourse } = useCourses();

const availableDictionaries = computed(() => {
    if (!props.word || !activeCourse.value) {
        return [];
    }

    const languageModule = getLanguageModule(activeCourse.value.targetLanguageCode);
    const primaryTranslationLanguageCode = activeCourse.value.translationLanguageCodes[0] || '';

    return languageModule.dictionaries
        .map(service => ({
            name: service.name,
            link: service.link(props.word as string, primaryTranslationLanguageCode)
        }))
        .filter(service => service.link !== null) as Array<{ name: string; link: string }>;
});
</script>
