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
                <div class="col-sm-4">
                    <div class="form-floating">
                        <textarea v-model="translationRussian" id="collocation-translate-rus" type="text"
                            class="form-control" style="height: 100px" placeholder="Leave a 🇷🇺 translation here" />
                        <label for="collocation-translate-rus">🇷🇺 Перевод</label>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-floating">
                        <textarea v-model="translationEnglish" id="collocation-translate-eng" type="text"
                            class="form-control" style="height: 100px" placeholder="Leave a 🇺🇸 translation here" />
                        <label for="collocation-translate-eng">🇺🇸 Перевод</label>
                    </div>
                </div>
            </div>
            <button type="submit" class="btn btn-primary">Добавить</button>
        </form>
    </div>
</template>
  
<script lang="ts">
import backendApi, { WordMeaningCollocationDto } from '@/api/backend-api';
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
            translationRussian: '',
            translationEnglish: ''
        };
    },
    methods: {
        showForm() {
            this.modalVisible = true;
        },
        resetForm() {
            this.modalVisible = false;
            this.pattern = '';
            this.translationRussian = '';
            this.translationEnglish = '';
        },
        submitForm() {
            const newItem = {
                pattern: this.pattern,
                translationRussian: this.translationRussian,
                translationEnglish: this.translationEnglish
            } as WordMeaningCollocationDto;

            try {
                backendApi.linkCollocation(this.meaningId, newItem)
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
