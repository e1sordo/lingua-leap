<template>
    <div>
        <div class="form-check form-switch text-start">
            <input class="form-check-input" type="checkbox" v-model="invertSpoilers" id="flexSwitchCheckDefault">
            <label class="form-check-label" for="flexSwitchCheckDefault">Invert spoilers</label>
        </div>

        <table class="table table-hover align-middle fs-4">
            <thead>
                <tr>
                    <th scope="col">#</th>
                    <th scope="col" style="width: 45%;">Word</th>
                    <th scope="col" @click="toggleMeaningLang()" style="cursor: pointer; width: 45%;">
                        Meaning
                    </th>
                    <th scope="col">(i)</th>
                </tr>
            </thead>
            <tbody class="table-group-divider">
                <tr v-for="(word, index) in shuffledWords" :key="word.id">
                    <th scope="row" class="fs-6">{{ index + 1 }}</th>
                    <th class="fw-normal" @click="toggleSpoiler(index)">
                        <span v-if="!invertSpoilers && spoilers[index]" class="spoiler pe-auto">
                            {{ word.fakeWord }}
                        </span>
                        <span v-else>{{ word.word }}</span>
                    </th>
                    <th class="fw-normal" @click="toggleSpoiler(index)">
                        <span v-if="invertSpoilers && spoilers[index]" class="spoiler pe-auto">
                            {{ word.fakeWord }}
                        </span>
                        <span v-else>{{ meaningValue ? word.englishVariant : word.russianVariant }}</span>
                    </th>
                    <th>
                        <word-context-menu :word="word.word">
                            <div class="btn btn-sm">(i)</div>
                        </word-context-menu>
                    </th>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script lang="ts">
import { ForeignWordDto } from '@/api/backend-api';
import { defineComponent, PropType, ref, watch } from 'vue';
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
        const meaningValue = ref(true); // true -- eng, false -- rus
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
        return { meaningValue, spoilers, invertSpoilers };
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
            this.meaningValue = !this.meaningValue;
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
