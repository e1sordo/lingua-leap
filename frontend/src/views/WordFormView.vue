<template>
  <div>
    <div class="container-md mb-5">
      <form @submit.prevent="submitForm" class="m-4">

        <div class="input-group input-group-lg mb-4">
          <span class="input-group-text" id="inputGroup-sizing-lg">Слово</span>
          <input v-model="form.word" id="word" type="text" required class="form-control fw-bolder" aria-label="Word input"
            aria-describedby="inputGroup-sizing-lg" @input="updatePageTitle">
        </div>

        <div v-for="(meaning, index) in form.meanings" :key="index" class="card text-start bg-transparent mb-4">
          <div class="card-body">

            <div class="d-flex justify-content-between align-items-center mb-4">
              <h5 class="card-title mb-0">Значение слова #{{ index + 1 }}</h5>

              <button type="button" v-if="form.meanings.length > 1" @click="removeMeaning(index)"
                class="btn btn-danger btn-sm">
                Удалить
              </button>
            </div>

            <div class="row g-3 mb-3">
              <div class="col-sm-6">
                <div class="input-group">
                  <span class="input-group-text">🇷🇺</span>
                  <input v-model="meaning.russianVariant" id="russianVariant" type="text" required class="form-control"
                    placeholder="Перевод на русский">
                </div>
              </div>
              <div class="col-sm-6">
                <div class="input-group">
                  <span class="input-group-text">🇬🇧</span>
                  <input v-model="meaning.englishVariant" id="englishVariant" type="text" required class="form-control"
                    placeholder="Translation into English">
                </div>
              </div>
            </div>

            <div class="mb-3">
              <label for="definition" class="form-label">Определение:</label>
              <input v-model="meaning.definition" id="definition" type="text" class="form-control"
                aria-label="Word definition" aria-describedby="basic-addon1">
            </div>

            <div class="mb-3 col-lg-4">
              <label for="vocabularyLists" class="form-label">Добавление в списки:</label>
              <select multiple v-model="meaning.lists" id="vocabularyLists" class="form-select">
                <option v-for="list in allVocabularyLists" :key="list.id" :value="list"
                  :selected="meaning.lists.map(l => l.id).includes(list.id)">
                  {{ list.name }}
                </option>
              </select>
            </div>

            <div class="mb-3">
              <label for="imageUrl" class="form-label">Изображение:</label>
              <input v-model="meaning.imageUrl" id="imageUrl" type="text" class="form-control" placeholder="URL"
                aria-label="Image URL" aria-describedby="basic-addon1">

              <div v-if="meaning.imageUrl" class="row mt-1">
                <div class="col-lg-3 col-md-6 col-sm-9">
                  <img :src="meaning.imageUrl" alt="Image" class="img-fluid w-100 img-thumbnail mt-2" />
                </div>
              </div>
            </div>

            <div class="mb-3">
              <label for="partOfSpeech" class="form-label">Часть речи:</label>
              <select v-model="meaning.pos" id="partOfSpeech" class="form-select">
                <option v-for="pos in partOfSpeechList" :key="pos.value" :value="pos.value">
                  {{ pos.label }}
                </option>
              </select>
            </div>

            <div v-if="meaning.pos === 'NOUN'" class="mb-3">
              <label for="grammaticalGender" class="form-label">Род существительного:</label>
              <div class="w-100" />
              <div class="btn-group" role="group" aria-label="Radio toggle for grammatial gender of noun">
                <template v-for="(gender, genderIndex) in genderList" :key="genderIndex">
                  <input type="radio" class="btn-check" :name="index + 'btnradio'" v-model="meaning.gender"
                    autocomplete="off" :value="gender.value" :id="index + 'grammaticalGender' + genderIndex">
                  <label class="btn btn-outline-primary" :for="index + 'grammaticalGender' + genderIndex">
                    {{ gender.label }}
                  </label>
                </template>
              </div>
            </div>


            <h5 class="card-title pt-3 pb-2">📖 Примеры употребления</h5>

            <div v-for="(context, contextIndex) in meaning.contexts" :key="contextIndex">

              <div class="row g-3 mb-3">
                <div class="col-sm-6">
                  <div class="form-floating">
                    <textarea v-model="context.sentence" :id="'context-original-' + contextIndex" type="text"
                      class="form-control" @input="handleContexSentenceInput(index, contextIndex)" style="height: 100px"
                      placeholder="Leave a sentence here" />
                    <label :for="'context-original-' + contextIndex">
                      Пример #{{ contextIndex + 1 }}. Исходная фраза
                    </label>
                  </div>
                </div>
                <div class="col-sm-6">
                  <div class="form-floating">
                    <textarea v-model="context.translation" :id="'context-translate-' + contextIndex" type="text"
                      class="form-control" style="height: 100px" placeholder="Leave a translation here" />
                    <label :for="'context-translate-' + contextIndex">Перевод</label>
                  </div>
                </div>
              </div>

              <div class="d-flex justify-content-between mb-3">
                <button :disabled="contextIndex !== meaning.contexts.length - 1" @click.prevent="addContext(index)"
                  type="button" class="btn btn-success">+ пример</button>
                <button v-if="meaning.contexts.length > 1" @click.prevent="removeContext(index, contextIndex)"
                  type="button" class="btn btn-danger">- удалить</button>
              </div>
            </div>

          </div>
        </div>

        <button type="button" class="btn btn-success" @click="addMeaning">
          Добавить новое значение слова
        </button>

        <div class="row g-3 my-4">
          <div class="col-auto">
            <div class="input-group mb-3">
              <span class="input-group-text" id="basic-addon1">Дата добавления:</span>
              <input v-model="form.addedDate" id="addedDate" type="date" class="form-control" />
            </div>
          </div>
          <div class="col-auto">
            <button type="submit" class="btn btn-primary mb-3">
              Добавить слово
            </button>
          </div>
        </div>

      </form>
    </div>
  </div>
</template>


<script lang="ts">
import backendApi, { AddNewWordRequestDto, VocabularyListDto } from '@/api/backend-api';
import { partOfSpeechMeta } from '@/constants';
import { defineComponent } from 'vue';

const sortedPosArray = Object.entries(partOfSpeechMeta)
  .map(([key, { label, printOrder }]) => ({ label, value: key, printOrder }))
  .sort((a, b) => a.printOrder - b.printOrder);

export default defineComponent({
  data() {
    return {
      allVocabularyLists: [] as VocabularyListDto[],
      form: {
        word: '',
        addedDate: new Date(),
        meanings: [
          {
            id: 0,
            pos: '',
            gender: 'MASCULINE',
            imageUrl: '',
            russianVariant: '',
            englishVariant: '',
            definition: '',
            frequency: 1,
            learningStatus: 'NEW',
            contexts: [{ id: 0, sentence: '', translation: '' }],
            lists: [] as VocabularyListDto[]
          },
        ],
      } as AddNewWordRequestDto
    }
  },
  setup() {
    const partOfSpeechList = sortedPosArray;
    const genderList = [
      { label: '🚹', value: 'MASCULINE' },
      { label: '🚺', value: 'FEMININE' },
      { label: 'Средний', value: 'NEUTER' },
      { label: 'Имя собственное', value: 'PROPER' },
    ];

    return { partOfSpeechList, genderList };
  },
  mounted() {
    this.fetchAllVocabularyLists();
  },
  methods: {
    async fetchAllVocabularyLists() {
      try {
        const response = await backendApi.getAllLists();
        const list = response.data;

        this.allVocabularyLists = list;

      } catch (error) {
        console.error(error);
      }
    },

    addMeaning() {
      this.form.meanings.push({
        id: 0,
        word: '',
        pos: '',
        gender: 'MASCULINE',
        imageUrl: '',
        russianVariant: '',
        englishVariant: '',
        definition: '',
        frequency: 1,
        learningStatus: 'NEW',
        contexts: [{ id: 0, sentence: '', translation: '' }],
        lists: [],
        addedDate: null
      });
    },
    removeMeaning(index: number) {
      if (this.form.meanings.length > 1) {
        this.form.meanings.splice(index, 1);
      }
    },
    addContext(meaningIndex: number) {
      this.form.meanings[meaningIndex].contexts.push({ id: 0, sentence: '', translation: '' });
    },
    removeContext(meaningIndex: number, contextIndex: number) {
      if (this.form.meanings[meaningIndex].contexts.length > 1) {
        this.form.meanings[meaningIndex].contexts.splice(contextIndex, 1);
      }
    },
    handleContexSentenceInput(meaningIndex: number, contextIndex: number) {
      const regex = /([.?!])(?!\s)/; // regex for '.', '!', '?' without trailing space
      const context = this.form.meanings[meaningIndex].contexts[contextIndex];
      const sentences = context.sentence.split(regex);

      if (sentences.length > 1 && context.translation.length === 0) {
        context.translation = sentences.slice(1).join('').trim().substring(1);
        context.sentence = sentences[0] + context.translation.slice(-1);
      }
    },
    resetForm() {
      this.form.word = '';
      this.form.meanings = [
        {
          id: 0,
          word: '',
          pos: '',
          gender: 'MASCULINE',
          imageUrl: '',
          russianVariant: '',
          englishVariant: '',
          definition: '',
          frequency: 1,
          learningStatus: 'NEW',
          contexts: [{ id: 0, sentence: '', translation: '' }],
          lists: [],
          addedDate: null
        },
      ];
      this.updatePageTitle();
    },
    submitForm() {
      const requestDto = {
        word: this.form.word,
        addedDate: new Date(this.form.addedDate),
        meanings: this.form.meanings.map((meaning) => ({
          id: meaning.id,
          word: meaning.word,
          pos: meaning.pos,
          gender: meaning.gender,
          imageUrl: meaning.imageUrl,
          russianVariant: meaning.russianVariant,
          englishVariant: meaning.englishVariant,
          definition: meaning.definition,
          frequency: 1,
          learningStatus: meaning.learningStatus,
          contexts: meaning.contexts.map((context) => ({
            id: context.id,
            sentence: context.sentence,
            translation: context.translation,
          })),
          lists: meaning.lists,
          addedDate: null
        })),
      };

      console.log(requestDto);

      try {
        backendApi.addNewWord(requestDto)
          .then(() => {
            this.increaseTotalWords();
            console.log("Word was added");
            this.resetForm();
          });
      } catch (ex) {
        console.log("Error", ex);
      }
    },
    increaseTotalWords() {
      this.$emit('updateTotalWords', (currentValue: number) => currentValue + 1);
    },
    updatePageTitle() {
      const titleAppend = this.form.word;
      document.title = `Add${titleAppend.length > 0 ? ' ' + titleAppend : ''}`;
    },
  }
});
</script>
