<template>
    <div class="position-relative">
        <div ref="hiddenCardTopElement" style="height: 30px; position: absolute; top: -30px; pointer-events: none;"></div>
        <div class="card text-center">
            <a @click="toggleCardAndShowAnswer" type="button">
                <img v-if="meaning.imageUrl" :src="meaning.imageUrl" class="card-img-top">
                <img v-else src="https://developers.elementor.com/docs/assets/img/elementor-placeholder-image.png"
                    class="opacity-25 card-img-top" />
            </a>

            <template v-if="!showAnswer">
                <div class="card-body">
                    <h1 class="card-title">{{ primaryTranslation(meaning, activeCourse) }}</h1>
                    <h3 v-if="secondaryTranslations.length > 0" class="card-title"
                        :class="{ 'spoiler': !showContexts }" @click="toggleSpoilers">
                        {{ showContexts ? secondaryTranslations.join(' • ') :
                            secondaryTranslations.map(() => '•••').join(' ') }}
                    </h3>

                    <hr v-if="meaning.collocations && meaning.collocations.length > 0"
                        class="border border-secondary border-2 opacity-75 my-4" />
                    <div>
                        <button v-for="collocation in meaning.collocations" :key="collocation.id" type="button"
                            class="btn btn-primary collocation-question-btn m-1">
                            {{ showContexts
                                ? orderedTranslations(collocation, activeCourse).map(t => t.text).join(' • ')
                                : primaryTranslation(collocation, activeCourse) }}
                        </button>
                    </div>

                    <template v-if="meaning.pos != 'ABBREVIATION'">
                        <hr v-if="meaning.definition" class="border border-primary border-3 opacity-75" />
                        <p v-if="meaning.definition" class="text-muted">{{ meaning.definition }}</p>
                    </template>
                </div>

                <div class="card-footer">
                    <div class="card card-body mb-2" v-for="(context, contextIndex) in meaning.contexts" :key="contextIndex"
                        :class="{ 'spoiler': !showContexts }" @click="toggleSpoilers">
                        {{ context.translation }}
                    </div>
                </div>
            </template>

            <template v-else>
                <div class="card-body">
                    <word-context-menu :word="meaning.word">
                        <h1 class="card-title" @click="speakWord(meaning.word)">
                            <small v-if="nounArticle" class="fw-lighter fst-italic pe-2">
                                {{ nounArticle }}
                            </small>
                            {{ meaning.word }}
                        </h1>
                    </word-context-menu>

                    <div class="d-flex justify-content-center align-items-center mx-md-4 my-2">
                        <small v-if="meaning.gender" class="pe-2">
                            <i v-if="meaning.gender == 'MASCULINE'" class="bi bi-gender-male"></i>
                            <i v-if="meaning.gender == 'FEMININE'" class="bi bi-gender-female"></i>
                        </small>
                        <small class="fw-lighter fst-italic">{{ meaning.pos }}</small>
                    </div>

                    <h4 v-for="entry in orderedTranslations(meaning, activeCourse)" :key="entry.code"
                        class="card-title py-1">
                        <small class="fw-lighter">{{ languageLabel(entry.code, languages) }}:</small>
                        {{ entry.text }}
                    </h4>

                    <hr v-if="meaning.definition" class="border border-primary border-3 opacity-75" />
                    <p v-if="meaning.definition" class="text-muted">{{ meaning.definition }}</p>

                    <hr v-if="meaning.collocations && meaning.collocations.length > 0"
                        class="border border-secondary border-2 opacity-75 my-4" />
                    <div class="my-3" v-for="(collocation, collocationIndex) in meaning.collocations"
                        :key="collocationIndex">
                        <p class="user-select-all mb-1" v-html="collocation.resolvedPattern"></p>
                        <span class="text-muted user-select-all">
                            {{ orderedTranslations(collocation, activeCourse).map(t => t.text).join(' • ') }}
                        </span>
                    </div>

                    <hr v-if="meaning.contexts && meaning.contexts.length > 0"
                        class="border border-secondary border-2 opacity-75 my-4" />
                    <div class="my-3" v-for="(context, contextIndex) in meaning.contexts" :key="contextIndex">
                        <p class="user-select-all mb-1"><strong>{{ context.sentence }}</strong></p>
                        <span class="text-muted user-select-all">{{ context.translation }}</span>
                    </div>
                </div>

                <div class="card-footer">
                    <div class="d-flex justify-content-between mx-md-4 my-2">
                        <button v-for="score in scores" :key="score.score" @click="submitAnswer(score.score)" type="button"
                            class="btn border-3" :class="'btn-outline-' + score.color" :disabled="buttonsDisabled">
                            {{ score.score }}
                        </button>
                    </div>

                    <ol class="list-group list-group-numbered text-start fw-lighter mt-4 mb-2">
                        <li>
                            <code>0</code>: "Total blackout", complete failure to recall the information.
                        </li>
                        <li>
                            <code>1</code>: Incorrect response, but upon seeing the correct answer it felt familiar.
                        </li>
                        <li>
                            <code>2</code>: Incorrect response, but upon seeing the correct answer it seemed easy to
                            remember.
                        </li>
                        <li>
                            <code>3</code>: Correct response, but required significant effort to recall.
                        </li>
                        <li>
                            <code>4</code>: Correct response, after some hesitation.
                        </li>
                        <li>
                            <code>5</code>: Correct response with perfect recall.
                        </li>
                    </ol>
                </div>
            </template>

        </div>

    </div>
</template>

<script setup lang="ts">
import api, { WordMeaningDto } from "@/api/backend-api";
import { useCourses } from '@/composables/useCourses';
import { getLanguageModule } from '@/languages';
import { languageLabel, orderedTranslations, primaryTranslation } from '@/utils/translations';
import { speak } from '@/utils/voice';
import WordContextMenu from '@/components/WordContextMenu.vue';
import { computed, PropType, inject, onMounted, ref } from 'vue';

const props = defineProps({
    meaning: {
        type: Object as PropType<WordMeaningDto>,
        required: true
    },
    onSubmit: {
        type: Object as PropType<() => void>,
        required: true
    }
});

const { activeCourse, activeCourseId, languages, loadLanguages } = useCourses();

onMounted(() => {
    loadLanguages();
});

const languageModule = computed(() =>
    getLanguageModule(activeCourse.value?.targetLanguageCode || '')
);

const nounArticle = computed(() => {
    if (!props.meaning.gender) {
        return '';
    }
    return languageModule.value.nounArticles[props.meaning.gender as keyof typeof languageModule.value.nounArticles] || '';
});

const secondaryTranslations = computed(() => {
    const codes = activeCourse.value?.translationLanguageCodes || [];
    return orderedTranslations(props.meaning, activeCourse.value)
        .filter(entry => entry.code !== codes[0])
        .map(entry => entry.text);
});

const decreaseTotalWordsToRepeatToday = inject('decreaseTotalWordsToRepeatToday') as () => void;

const scores = [
    { score: 0, color: 'secondary' },
    { score: 1, color: 'danger' },
    { score: 2, color: 'warning' },
    { score: 3, color: 'primary' },
    { score: 4, color: 'info' },
    { score: 5, color: 'success' }
];

const showContexts = ref(false);
const showAnswer = ref(false);
const buttonsDisabled = ref(false);

const hiddenCardTopElement = ref<HTMLDivElement>();

const toggleSpoilers = () => {
    showContexts.value = !showContexts.value;
};

const speakWord = (text: string) => {
    speak(text, languageModule.value, { courseId: activeCourseId.value ?? undefined });
};

const toggleCardAndShowAnswer = () => {
    showAnswer.value = true;
    speakWord(props.meaning.word);
};

const submitAnswer = (score: number) => {
    if (activeCourseId.value === null) {
        return;
    }

    buttonsDisabled.value = true;
    api.scoreWordMeaning(activeCourseId.value, props.meaning.id, score).then(() => {
        showAnswer.value = false;
        showContexts.value = false;

        decreaseTotalWordsToRepeatToday();

        buttonsDisabled.value = false;

        props.onSubmit();

        hiddenCardTopElement.value?.scrollIntoView({ behavior: 'smooth' });
    });
};
</script>

<style>
.main-word {
    font-weight: bold;
}

.collocation-question-btn .main-word {
    filter: blur(4px);
}
</style>
