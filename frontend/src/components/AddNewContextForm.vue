<template>
    <div>
        <button class="btn btn-secondary btn-sm" @click="showForm">Добавить предложение</button>

        <form @submit.prevent="submitForm" @hidden="resetForm" v-if="modalVisible">
            <div class="row mb-3 mt-2">
                <div class="col-sm-6">
                    <div class="form-floating">
                        <textarea v-model="sentence" id="context-original" type="text" class="form-control"
                            @input="handleContexSentenceInput" style="height: 100px" placeholder="Leave a sentence here" />
                        <label for="context-original">Исходная фраза</label>
                    </div>
                </div>
                <div class="col-sm-6">
                    <div class="form-floating">
                        <textarea v-model="translation" id="context-translate" type="text" class="form-control"
                            style="height: 100px" placeholder="Leave a translation here" />
                        <label for="context-translate">Перевод</label>
                    </div>
                </div>
            </div>
            <button type="submit" class="btn btn-primary">Добавить</button>
        </form>
    </div>
</template>
  
<script lang="ts">
import backendApi, { WordMeaningContextDto } from '@/api/backend-api';
import { defineComponent, PropType } from 'vue';

export default defineComponent({
    props: {
        meaningId: {
            type: Number as PropType<number>,
            required: true,
        },
        onAddItem: {
            type: Function as PropType<(item: WordMeaningContextDto) => void>,
            required: true,
        },
    },
    data() {
        return {
            modalVisible: false,
            sentence: '',
            translation: '',
        };
    },
    methods: {
        showForm() {
            this.modalVisible = true;
        },
        resetForm() {
            this.modalVisible = false;
            this.sentence = '';
            this.translation = '';
        },
        submitForm() {
            const newItem = {
                sentence: this.sentence,
                translation: this.translation
            } as WordMeaningContextDto;

            try {
                backendApi.linkContext(this.meaningId, newItem)
                    .then((resp) => {
                        this.onAddItem(resp.data);
                        console.log("Word was added");
                        this.resetForm();
                    });
            } catch (ex) {
                console.log("Error", ex);
            }
        },
        handleContexSentenceInput() {
            const regex = /\.(?!\s)/; // regex for dot without trailing space
            const sentences = this.sentence.split(regex);

            if (sentences.length > 1 && this.translation.length === 0) {
                this.sentence = sentences[0] + '.';
                this.translation = sentences.slice(1).join('.').trim();
            }
        },
    },
});
</script>