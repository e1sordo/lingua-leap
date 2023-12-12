<template>
  <div>
    <div class="container-md mb-5">

      <div class="mb-4">
        <h1 class="display-1">{{ wordDetail.word }}</h1>
      </div>

      <p>
        <a :href="`https://www.spanishdict.com/translate/${wordDetail.word}`" class="btn btn-outline-primary btn-lg m-2"
          target="_blank">
          Span¡shD!ctionary
        </a>
        <a :href="`https://youglish.com/pronounce/${wordDetail.word}/spanish`" class="btn btn-outline-success btn-lg m-2"
          target="_blank">
          YouGlish
        </a>
      </p>

      <div v-for="(meaning, index) in wordDetail.meanings" :key="index" class="card text-start bg-transparent mb-4">
        <div class="card-body">

          <div class="d-flex justify-content-between align-items-center mb-4">
            <h5 class="card-title mb-0">Значение слова #{{ index + 1 }}</h5>
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

          <div v-if="meaning.definition" class="mb-3">
            {{ meaning.definition }}
          </div>

          <div v-if="meaning.imageUrl" class="row mt-1 mb-3">
            <div class="col-lg-3 col-md-6 col-sm-9">
              <img :src="meaning.imageUrl" alt="Image" class="img-fluid w-100 img-thumbnail mt-2" />
            </div>
          </div>

          <div class="mb-3">
            <label for="partOfSpeech" class="form-label">Часть речи:</label>
            {{ meaning.pos }}
          </div>

          <div v-if="meaning.pos === 'NOUN'" class="mb-3">
            <label for="grammaticalGender" class="form-label">Род существительного:</label>
            {{ meaning.gender }}
          </div>


          <h5 class="card-title pt-3 pb-2">📖 Примеры употребления</h5>

          <div class="mx-lg-2">
            <figure v-for="context in meaning.contexts" :key="context.id">
              <blockquote class="blockquote">
                <p>{{ context.sentence }}</p>
              </blockquote>
              <figcaption class="blockquote-footer">
                {{ context.translation }}
              </figcaption>
            </figure>

            <AddNewContextForm :meaningId="meaning.id" :onAddItem="(item) => addContext(index, item)" />
          </div>

        </div>
      </div>


      <div class="row g-3 my-4">
        <div class="col-auto">
          Дата добавления: {{ wordDetail.addedDate }}
        </div>
      </div>

    </div>
  </div>
</template>

<script lang="ts">
import api, { ForeignWordDetailDto, WordMeaningContextDto } from "@/api/backend-api";
import AddNewContextForm from '@/components/AddNewContextForm.vue';
import { partOfSpeechMeta } from '@/constants';
import { convertDateToSinceString } from '@/utils/convertDateToSinceString';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'WordDetailView',
  components: {
    AddNewContextForm
  },
  data() {
    return {
      wordDetail: {} as ForeignWordDetailDto
    };
  },
  mounted() {
    this.fetchDetails();
  },
  setup() {
    return { partOfSpeechMeta, convertDateToSinceString };
  },
  methods: {
    async fetchDetails() {
      try {
        const response = await api.getByName(this.$route.params.word as string);
        this.wordDetail = response.data;
        document.title = document.title + ` ${this.wordDetail.word}`;
      } catch (error) {
        console.error(error);
      }
    },
    addContext(meaningIndex: number, newContext: WordMeaningContextDto) {
      console.log(meaningIndex, newContext);
      this.wordDetail.meanings[meaningIndex].contexts.push(newContext);
    },
  }
});
</script>
