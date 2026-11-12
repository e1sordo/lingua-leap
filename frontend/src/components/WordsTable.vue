<template>
    <div>
        <div class="form-check form-switch text-start">
            <input class="form-check-input" type="checkbox" v-model="invertSpoilers" id="flexSwitchCheckDefault">
            <label class="form-check-label" for="flexSwitchCheckDefault">Invert spoilers</label>
        </div>

        <table class="table table-hover align-middle">
            <thead>
                <tr>
                    <th scope="col">#</th>
                    <th scope="col" style="width: 45%; max-width: 45%;">Word</th>
                    <th scope="col" @click="toggleMeaningLang()" style="cursor: pointer; width: 45%; max-width: 45%;">
                        Meaning ({{ selectedTranslationLanguageCode }})
                    </th>
                    <th scope="col"></th>
                </tr>
            </thead>
            <tbody class="table-group-divider">
                <tr v-for="(word, index) in shuffledWords" :key="word.id">
                    <th scope="row">{{ index + 1 }}</th>
                    <th class="fw-normal text-break" @click="toggleSpoiler(index)">
                        <span v-if="!invertSpoilers && spoilers[index]" class="spoiler pe-auto">
                            {{ word.fakeWord }}
                        </span>
                        <span v-else>
                            <small v-if="word.gender" class="pe-1">
                                <i v-if="word.gender == 'MASCULINE'" class="bi bi-gender-male male-gender"></i>
                                <i v-if="word.gender == 'FEMININE'" class="bi bi-gender-female female-gender"></i>
                            </small>
                            <span v-html="word.word"></span>
                        </span>
                    </th>
                    <th class="fw-normal text-break" @click="toggleSpoiler(index)">
                        <span v-if="invertSpoilers && spoilers[index]" class="spoiler pe-auto">
                            {{ word.fakeWord }}
                        </span>
                        <span v-else>
                            {{ translation(word, selectedTranslationLanguageCode) }}
                        </span>
                    </th>
                    <th>
                        <word-context-menu :word="word.word">
                            <div class="btn btn-sm"><i class="bi bi-caret-down-fill"></i></div>
                        </word-context-menu>
                    </th>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script lang="ts">
import { ForeignWordDto } from '@/api/backend-api';
import { useCourses } from '@/composables/useCourses';
import { translation } from '@/utils/translations';
import { computed, defineComponent, PropType, ref, watch } from 'vue';
import WordContextMenu from '@/components/WordContextMenu.vue';

export default defineComponent({
    name: 'WordsTable',
    components: { WordContextMenu },
    props: {
        refresh: Boolean,
        data: {
            type: Array as PropType<Array<ForeignWordDto>>,
            required: true,
            default: () => ([])
        }
    },
    setup(props) {
        const { activeCourse } = useCourses();

        const translationLanguageCodes = computed(() =>
            activeCourse.value?.translationLanguageCodes || []
        );

        const selectedTranslationLanguageCode = ref('');
        watch(translationLanguageCodes, (codes) => {
            if (codes.length > 0 && !codes.includes(selectedTranslationLanguageCode.value)) {
                selectedTranslationLanguageCode.value = codes[0];
            }
        }, { immediate: true });

        const invertSpoilers = ref(false);
        const spoilers = ref(new Array(50).fill(true));

        watch(() => props.data, (newData) => {
            if (newData.length > 0) {
                spoilers.value = new Array(newData.length).fill(true);
            }
        });
        watch(() => props.refresh, () => {
            spoilers.value = new Array(props.data.length).fill(true);
        })
        return { selectedTranslationLanguageCode, translationLanguageCodes, spoilers, invertSpoilers, translation };
    },
    computed: {
        shuffledWords() {
            this.refresh;
            return this.data
                .map((value) => ({
                    ...value,
                    fakeWord: this.generateRandomString(),
                    sort: Math.random()
                }))
                .sort((a, b) => a.sort - b.sort);
        }
    },
    methods: {
        toggleSpoiler(index: number) {
            this.spoilers[index] = !this.spoilers[index];
        },
        toggleMeaningLang() {
            const codes = this.translationLanguageCodes;
            if (codes.length < 2) {
                return;
            }
            const currentIndex = codes.indexOf(this.selectedTranslationLanguageCode);
            this.selectedTranslationLanguageCode = codes[(currentIndex + 1) % codes.length];
        },
        generateRandomString() {
            const length = Math.floor(Math.random() * 8) + 4;
            return Array.from({ length }, () => {
                // from 'a' to 'z'
                const charCode = Math.floor(Math.random() * 26) + 97; // 97 - code of 'a'
                return String.fromCharCode(charCode);
            }).join('');
        }
    }
});
</script>

<style scoped>
.male-gender {
    color: rgba(5, 187, 54, 0.8);
}

.female-gender {
    color: rgba(234, 64, 223, 0.8);
}
</style>
