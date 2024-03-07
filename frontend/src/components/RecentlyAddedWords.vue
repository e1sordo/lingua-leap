<template>
    <div>
        <template v-for="word in sortedData" :key="word.id">
            <div class="m-2 m-md-3 d-inline-flex">
                <div class="text-decoration-none rounded learning-level-side">
                    <i class="bi mx-2" :class="iconsByLearningStatus[word.learningStatus]"></i>
                    <div class="fs-5 fw-normal py-2 px-3 badge word-text" :class="'pos-' + word.pos.toLowerCase()">
                        <word-context-menu :word="word.word">
                            <small v-if="word.gender" class="pe-2">
                                <i v-if="word.gender == 'MASCULINE'" class="bi bi-gender-male male-gender"></i>
                                <i v-if="word.gender == 'FEMININE'" class="bi bi-gender-female female-gender"></i>
                            </small>
                            <span v-html="word.word"></span>
                        </word-context-menu>
                    </div>
                    <i v-if="listView" class="bi bi-folder-minus mx-2" type="button" data-bs-toggle="modal"
                        :data-bs-target="'#confirmationModal' + word.id"></i>
                </div>
            </div>

            <ActionModalWindow v-if="listView" :id="'confirmationModal' + word.id" title="Delete from list">
                <div class="modal-body">
                    <p>Are you sure you want to delete word from this list?</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                        Close
                    </button>
                    <button type="button" class="btn btn-danger" data-bs-dismiss="modal"
                        @click="$emit('removeFromList', word)">
                        Delete
                    </button>
                </div>
            </ActionModalWindow>
        </template>
    </div>
</template>

<script>
import { partOfSpeechMeta } from '@/constants';
import { defineComponent } from 'vue';
import WordContextMenu from '@/components/WordContextMenu.vue';
import ActionModalWindow from '@/components/ActionModalWindow.vue';

export default defineComponent({
    name: 'RecentlyAddedWords',
    components: {
        WordContextMenu, ActionModalWindow
    },
    props: {
        data: {
            type: Array,
            required: true,
            default: () => ([])
        },
        listView: {
            type: Boolean,
            required: false,
            default: false
        }
    },
    setup() {
        const iconsByLearningStatus = {
            NEW: 'bi-reception-0',
            TO_STUDY: 'bi-reception-2',
            KNOWN: 'bi-reception-4'
        }

        return { iconsByLearningStatus }
    },
    computed: {
        sortedData() {
            const sortedArray = [...this.data];
            sortedArray.sort((a, b) => partOfSpeechMeta[a.pos].printOrder - partOfSpeechMeta[b.pos].printOrder);
            return sortedArray;
        }
    }
});
</script>

<style scoped>
.learning-level-side {
    background-color: rgba(83, 104, 124, 0.14);
}

.d-inline-flex {
    display: inline-flex;
}

.word-text {
    --word-pos-bg-opacity: 0.85;
}

@media (prefers-color-scheme: dark) {
    .word-text {
        --word-pos-bg-opacity: 0.55;
    }
}

.pos-noun {
    background-color: rgba(11, 87, 177, var(--word-pos-bg-opacity));
}

.pos-verb {
    background-color: rgba(6, 195, 63, var(--word-pos-bg-opacity));
}

.pos-adjective {
    background-color: rgba(166, 28, 37, var(--word-pos-bg-opacity));
}

.pos-adverb {
    background-color: rgba(176, 158, 8, var(--word-pos-bg-opacity));
}

.pos-pronoun {
    background-color: rgba(119, 8, 227, var(--word-pos-bg-opacity));
}

.pos-preposition {
    background-color: rgba(138, 138, 138, var(--word-pos-bg-opacity));
}

.pos-conjunction {
    background-color: rgba(235, 122, 11, var(--word-pos-bg-opacity));
}

.pos-interjection {
    background-color: rgba(238, 10, 216, var(--word-pos-bg-opacity));
}

.pos-determiner {
    background-color: rgba(92, 62, 32, var(--word-pos-bg-opacity));
}

.pos-phrase {
    background-color: rgba(28, 155, 166, var(--word-pos-bg-opacity));
}

.pos-abbreviation {
    background-color: rgba(124, 166, 28, var(--word-pos-bg-opacity));
}

.male-gender {
    color: rgb(10, 255, 76);
}

.female-gender {
    color: rgb(255, 133, 247);
}
</style>
