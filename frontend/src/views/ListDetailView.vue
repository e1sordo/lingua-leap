<template>
    <div>
        <div class="container-md mb-5">

            <div class="mb-4">
                <h1 class="display-3">{{ listDetail.name }}</h1>
            </div>

            <p>Список состоит из {{ listCount }} слов</p>

            <RecentlyAddedWords :data="listDetail.words" />

            <hr />

            <div class="row justify-content-center">
                <div class="col-lg-7">
                    <WordsTable :data="listDetail.words" :refresh="shuffleRefreshingBit" />
                    <button class="btn btn-sm btn-outline-primary" @click="toggleShuffleBit()">Shuffle</button>
                </div>
            </div>

            <hr />

            <div class="row justify-content-center">
                <div class="col-lg-9">

                    <figure v-for="context  in contexts" :key="context.id">
                        <blockquote class="blockquote">
                            <p>{{ context.sentence }}</p>
                        </blockquote>
                        <figcaption class="blockquote-footer">
                            {{ context.translation }}
                        </figcaption>
                    </figure>

                </div>
            </div>

            <hr />

            <div class="row justify-content-center">
                <div class="col-lg-9 fs-3">
                    <article style="font-family: 'Athelas', arial;">
                        <template v-for="context  in contexts" :key="context.id">
                            {{ context.sentence + ' 💃🏻 ' }}
                        </template>
                    </article>
                </div>
            </div>

        </div>
    </div>
</template>

<script lang="ts">
import api, { VocabularyListDetailDto, WordMeaningContextDto } from "@/api/backend-api";
import RecentlyAddedWords from '@/components/RecentlyAddedWords.vue';
import WordsTable from '@/components/WordsTable.vue';
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'ListDetailView',
    components: {
        RecentlyAddedWords, WordsTable
    },
    data() {
        return {
            shuffleRefreshingBit: true,
            listCount: 0,
            listDetail: {} as VocabularyListDetailDto,
            contexts: [] as WordMeaningContextDto[]
        };
    },
    mounted() {
        this.fetchDetails();
    },
    methods: {
        async fetchDetails() {
            try {
                let response;

                if (this.$route.path.includes('/pos/')) {
                    response = await api.getListByPos(this.$route.params.pos as string);
                } else if (this.$route.path.includes('/date/')) {
                    response = await api.getListByDate(this.$route.params.date as string);
                } else {
                    response = await api.getListById(parseInt(this.$route.params.id as string));
                }

                this.listDetail = response.data;
                this.listCount = this.listDetail.words.length;

                this.contexts = this.listDetail.words
                    .flatMap(word => word.contexts)
                    .map(context => ({ context, sort: Math.random() }))
                    .sort((a, b) => a.sort - b.sort)
                    .map(({ context }) => context);
            } catch (error) {
                console.error(error);
            }
        },
        toggleShuffleBit() {
            this.shuffleRefreshingBit = !this.shuffleRefreshingBit;
        }
    }
});
</script>
