<template>
  <div>
    <div class="container-sm">
      <div class="row justify-content-center">
        <div class="col-lg-4 text-start">

          <div v-if="!trainingCompleted">
            <div v-if="!trainingStarted">
              <!-- Форма настроек -->
              <form @submit.prevent="startTraining">
                <div class="mb-3">
                  <label for="totalVariants" class="form-label">Всего аббревиатур</label>
                  <input type="number" v-model="totalVariants" id="totalVariants" class="form-control" required />
                </div>

                <div class="mb-3">
                  <label for="trainingMode" class="form-label">Режим тренировки</label>
                  <select v-model="trainingMode" id="trainingMode" class="form-select" required>
                    <option value="audio">Восприятие на слух</option>
                    <option value="visual">Визуальное восприятие</option>
                  </select>
                </div>

                <div class="mb-3">
                  <div class="form-group" v-if="trainingMode === 'audio' && voiceList.length">
                    <label for="voices" class="form-label">Выберите голос</label>
                    <select id="voices" v-model="selectedVoice" class="form-control">
                      <option v-for="(voice, index) in voiceList" :key="index" :data-lang="voice.lang" :value="index">
                        {{ voice.name }} ({{ voice.lang }})
                      </option>
                    </select>
                  </div>
                </div>

                <button class="btn btn-primary mt-3" type="submit">Начать тренировку</button>
              </form>
            </div>

            <div v-else>
              <!-- Тренировка -->
              <div v-if="currentAbbreviation !== null">
                <p v-if="trainingMode === 'audio'" @click="speak(currentAbbreviation)">
                  #{{ trainingResults.length + 1 }}/{{ totalVariants }}. Слушайте аббревиатуру...
                </p>
                <p v-else>Введите аббревиатуру в текстовое поле: {{ currentAbbreviation }}</p>

                <div class="input-group input-group-lg">
                  <input v-model="userInput" ref="userInputField" class="form-control"
                    :class="{ 'is-valid': trainingStarted && previousStatus, 'is-invalid': trainingResults.length > 0 && !previousStatus }"
                    type="text" @keyup.enter="checkAnswerAndNextNumber" aria-describedby="button-user-input" autofocus />
                  <button class="btn btn-outline-secondary" type="button" id="button-user-input"
                    @click="checkAnswerAndNextNumber">
                    Подтвердить
                  </button>
                </div>

              </div>
            </div>
          </div>

          <!-- Результаты тренировки -->
          <div v-if="trainingCompleted" class="mt-3">
            <h3>Результаты тренировки</h3>

            <p class="my-3" v-if="trainingResults.filter(r => !r.correct).length == 0">
              💯 Всё верно! 👏
            </p>

            <table class="table table-bordered text-center">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Число</th>
                  <th>Ответ</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(result, index) in trainingResults" :key="index">
                  <td @click="speak(index + 1)">{{ index + 1 }}</td>
                  <td @click="speak(result.original)">{{ result.original }}</td>
                  <td @click="speak(result.answer)">{{ result.answer }}</td>
                  <td>{{ result.correct ? '✅' : '❌' }}</td>
                </tr>
              </tbody>
            </table>

            <button class="btn btn-primary" @click="finishTraining">Вернуться</button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

interface Result {
  original: string;
  answer: string;
  correct: boolean;
}

const items = [
  "JDK", // Java Development Kit
  "JPA", // Java Persistence API
  "SMI", // Salario Mínimo Interprofesional
];

export default defineComponent({
  name: 'ListenAbbreviationsView',
  data() {
    return {
      trainingStarted: false,
      trainingCompleted: false,
      totalVariants: 20,
      trainingMode: 'audio',
      previousStatus: false,

      previousAbbreviation: '',
      currentAbbreviation: '',

      userInput: '',
      showResult: false,
      resultMessage: '',
      trainingResults: [] as Result[],

      selectedVoice: 0,
      synth: window.speechSynthesis,
      voiceList: [] as SpeechSynthesisVoice[],
      speech: new window.SpeechSynthesisUtterance()
    };
  },
  mounted() {
    this.voiceList = this.synth.getVoices().filter(voice => voice.lang.startsWith('es-'));
  },
  methods: {
    startTraining() {
      this.trainingStarted = true;
      this.generateRandomAbbreviation();
    },
    finishTraining() {
      this.trainingStarted = false;
      this.trainingCompleted = false;
      this.trainingResults = [];
    },
    generateRandomAbbreviation() {
      this.previousAbbreviation = this.currentAbbreviation + '';

      this.currentAbbreviation = items[items.length * Math.random() | 0];
      while (this.currentAbbreviation === this.previousAbbreviation) {
        this.currentAbbreviation = items[items.length * Math.random() | 0];
      }

      if (this.trainingMode === 'audio') {
        this.speak(this.currentAbbreviation);
      }

      this.userInput = '';
    },
    checkAnswerAndNextNumber() {
      const originalAbbreviation = this.currentAbbreviation.toString();
      const userAnswer = this.userInput.toString().trim().toUpperCase();

      if (userAnswer.length > 0) {
        const isCorrect = originalAbbreviation === userAnswer;
        this.previousStatus = isCorrect;
        this.showResult = true;

        this.trainingResults.push({
          original: originalAbbreviation,
          answer: userAnswer,
          correct: isCorrect
        });

        this.nextAbbreviation();
      }
    },
    nextAbbreviation() {
      if (Object.keys(this.trainingResults).length === this.totalVariants) {
        this.trainingCompleted = true;
      } else {
        this.generateRandomAbbreviation();
        const userInputRef = this.$refs.userInputField as HTMLFormElement;
        userInputRef.focus();
      }
    },
    speak(num: number | string) {
      this.speech.text = num.toString();
      this.speech.voice = this.voiceList[this.selectedVoice];
      this.speech.pitch = Math.random() * (1.5 - 0.7) + 0.7;
      this.speech.rate = Math.random() * (1.3 - 0.9) + 0.9;
      this.synth.speak(this.speech);
    }
  }
});
</script>
