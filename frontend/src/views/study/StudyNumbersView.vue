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
                                    <label for="totalVariants" class="form-label">Всего чисел</label>
                                    <input type="number" v-model="totalVariants" id="totalVariants" class="form-control"
                                        required />
                                </div>

                                <div class="mb-3">
                                    <label for="numericType" class="form-label">Тип числительных</label>
                                    <select v-model="numericType" id="numericType" class="form-select" required>
                                        <option value="cardinal">Количественные (cardinal)</option>
                                        <option value="ordinal">Порядковые (ordinal)</option>
                                    </select>
                                </div>

                                <div class="mb-3">
                                    <label for="rangeStart" class="form-label">Начальное значение</label>
                                    <input type="number" v-model="rangeStart" id="rangeStart" class="form-control"
                                        required />
                                </div>

                                <div class="mb-3">
                                    <label for="rangeEnd" class="form-label">Конечное значение</label>
                                    <input type="number" v-model="rangeEnd" id="rangeEnd" class="form-control" required />
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
                                            <option v-for="(voice, index) in voiceList" :key="index" :data-lang="voice.lang"
                                                :value="index">
                                                {{ voice.name }} ({{ voice.lang }})
                                            </option>
                                        </select>
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <div class="form-check form-switch">
                                        <input id="voiceModificationCheck" v-model="voiceModificationCheck"
                                            class="form-check-input" type="checkbox" role="switch" checked>
                                        <label class="form-check-label" for="voiceModificationCheck">
                                            Модификация голоса
                                        </label>
                                    </div>
                                </div>

                                <button class="btn btn-primary mt-3" type="submit">Начать тренировку</button>
                            </form>
                        </div>

                        <div v-else>
                            <!-- Тренировка -->
                            <div v-if="currentNumber !== null">
                                <p @click="speakNumber(currentNumber)">
                                    #{{ trainingResults.length + 1 }}/{{ totalVariants }}.
                                    <template v-if="trainingMode === 'audio'">
                                        Слушайте число...
                                    </template>
                                    <template v-else>
                                        Введите это число:<br />
                                        <span class="fs-5">{{ numberToWords(currentNumber, numericType == 'ordinal')
                                        }}</span>
                                    </template>
                                </p>

                                <div class="input-group input-group-lg">
                                    <input v-model="userInput" ref="userInputField" class="form-control"
                                        :class="{ 'is-valid': trainingStarted && previousStatus, 'is-invalid': trainingResults.length > 0 && !previousStatus }"
                                        type="number" @keyup.enter="checkAnswerAndNextNumber"
                                        aria-describedby="button-user-input" autofocus />
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
                                    <td @click="speakNumber(index + 1)">{{ index + 1 }}</td>
                                    <td @click="speakNumber(result.original)">{{ result.original }}</td>
                                    <td @click="speakNumber(result.answer)">{{ result.answer }}</td>
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

<script setup lang="ts">
import { numberToWords } from '@/utils/numberToWords';
import { speak, voiceList } from '@/utils/voice';
import { ref } from 'vue';

interface Result {
    original: string;
    answer: string;
    correct: boolean;
}

const trainingStarted = ref(false);
const trainingCompleted = ref(false);
const totalVariants = ref(20);
const numericType = ref('cardinal');
const rangeStart = ref(1);
const rangeEnd = ref(100);
const trainingMode = ref('audio');
const previousStatus = ref(false);
const currentNumber = ref(0);
const previousNumber = ref(0);
const userInput = ref('');
const showResult = ref(false);
// const resultMessage = ref('');
const trainingResults = ref([] as Result[]);

const userInputField = ref(null);

const selectedVoice = ref(0);
const voiceModificationCheck = ref(false);

const startTraining = () => {
    trainingStarted.value = true;
    prepareNextNumber();
};

const finishTraining = () => {
    trainingStarted.value = false;
    trainingCompleted.value = false;
    trainingResults.value = [];
};

const prepareNextNumber = () => {
    previousNumber.value = currentNumber.value

    currentNumber.value = generateRandomNumber(previousNumber.value);

    if (trainingMode.value === 'audio') {
        speakNumber(currentNumber.value);
    }

    userInput.value = '';
};

const generateRandomNumber = (previousResult: number) => {
    const min = rangeStart.value;
    const max = rangeEnd.value;

    const totalSectors = 10;
    const sectorSize = Math.ceil((max - min) / totalSectors);

    const previousSectorIndex = Math.floor(previousResult / sectorSize);

    let newRandomNumber = Math.floor(Math.random() * (max - min + 1)) + min;
    let newSectorIndex = Math.floor(newRandomNumber / sectorSize);

    // prevent infinite loop
    const maxAttemptsToGenerateNewNumber = 100;

    let attempts = 0;
    while (newSectorIndex === previousSectorIndex || newRandomNumber % 10 === previousResult % 10) {
        newRandomNumber = Math.floor(Math.random() * (max - min + 1)) + min;
        newSectorIndex = Math.floor(newRandomNumber / sectorSize);

        attempts++;
        if (attempts < maxAttemptsToGenerateNewNumber) {
            break;
        }
    }

    return newRandomNumber;
};

const checkAnswerAndNextNumber = () => {
    const originalNumber = currentNumber.value.toString();
    const userAnswer = userInput.value.toString().trim();

    if (userAnswer.length > 0) {
        const isCorrect = originalNumber === userAnswer;
        previousStatus.value = isCorrect;
        showResult.value = true;

        trainingResults.value.push({
            original: originalNumber,
            answer: userAnswer,
            correct: isCorrect
        });

        nextNumber();
    }
};

const nextNumber = () => {
    if (Object.keys(trainingResults.value).length === totalVariants.value) {
        trainingCompleted.value = true;
    } else {
        prepareNextNumber();
        const userInputRef = userInputField.value;
        if (userInputRef) {
            (userInputRef as HTMLFormElement).focus();
        }
    }
};

const speakNumber = (number: number | string) => {
    const input = numericType.value == 'ordinal' ? number + 'º' : number;
    speak(input, selectedVoice.value, voiceModificationCheck.value);
}
</script>
