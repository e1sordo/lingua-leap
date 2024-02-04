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
                    <h1 class="card-title">{{ meaning.russianVariant }}</h1>
                    <h3 class="card-title" :class="{ 'spoiler': !showContexts }" @click="toggleSpoilers">
                        {{ showContexts ? meaning.englishVariant : meaning.russianVariant }}
                    </h3>

                    <hr class="border border-secondary border-2 opacity-75 my-4" />
                    <div>
                        <button v-for="collocation in meaning.collocations" :key="collocation.id" type="button"
                            class="btn btn-primary collocation-question-btn m-1">
                            {{ showContexts ? collocation.translationEnglish : collocation.translationRussian }}
                        </button>
                    </div>

                    <hr v-if="meaning.definition" class="border border-primary border-3 opacity-75" />
                    <p v-if="meaning.definition" class="text-muted">{{ meaning.definition }}</p>
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
                        <h1 class="card-title" @click="speak(meaning.word)">
                            <small v-if="meaning.gender">
                                <i v-if="meaning.gender == 'MASCULINE'" class="bi bi-gender-male"></i>
                                <i v-if="meaning.gender == 'FEMININE'" class="bi bi-gender-female"></i>
                            </small>
                            {{ meaning.word }}
                        </h1>
                    </word-context-menu>

                    <h4 class="card-title py-1">🇬🇧 {{ meaning.englishVariant }}</h4>
                    <h4 class="card-title py-1">🇷🇺 {{ meaning.russianVariant }}</h4>

                    <hr v-if="meaning.definition" class="border border-primary border-3 opacity-75" />
                    <p v-if="meaning.definition" class="text-muted">{{ meaning.definition }}</p>

                    <hr v-if="meaning.collocations && meaning.collocations.length > 0"
                        class="border border-secondary border-2 opacity-75 my-4" />
                    <div class="my-3" v-for="(collocation, collocationIndex) in meaning.collocations"
                        :key="collocationIndex">
                        <p class="user-select-all mb-1" v-html="collocation.resolvedPattern" />
                        <span class="text-muted user-select-all">
                            🇬🇧 {{ collocation.translationEnglish }} (🇷🇺 {{ collocation.translationRussian }})
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
                            class="btn border-3" :class="'btn-outline-' + score.color">
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
import WordContextMenu from '@/components/WordContextMenu.vue';
import { speak } from '@/utils/voice';
import { PropType, inject, ref } from 'vue';

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

const toggleCardAndShowAnswer = () => {
    showAnswer.value = true;
    speak(props.meaning.word);
};

const submitAnswer = (score: number) => {
    buttonsDisabled.value = true;
    api.scoreWordMeaning(props.meaning.id, score).then(() => {
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