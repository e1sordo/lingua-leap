<template>
    <div class="dropdown">
        <div aria-expanded="false" data-bs-toggle="dropdown" type="button">
            <slot></slot>
        </div>

        <ul class="dropdown-menu">
            <li>
                <router-link :to="`/words/${word}`" class="dropdown-item">
                    Detail
                </router-link>
            </li>

            <li v-if="customActions?.length || 0 > 0">
                <hr class="dropdown-divider" />
            </li>
            <li v-if="customActions?.length || 0 > 0">
                <h6 class="dropdown-header">Custom actions</h6>
            </li>
            <li v-for="(action, index) in customActions" :key="index">
                <a class="dropdown-item" :href="action.link" target="_blank">
                    {{ action.name }}
                </a>
            </li>

            <li>
                <hr class="dropdown-divider" />
            </li>
            <li>
                <h6 class="dropdown-header">Tools</h6>
            </li>
            <li v-for="(service, id) in dictionaryServices" :key="id">
                <a class="dropdown-item" :href="service.link(word!!)" target="_blank">
                    {{ service.name }}
                    <i class="bi bi-box-arrow-up-right text-primary"></i>
                </a>
            </li>
        </ul>
    </div>
</template>

<script setup lang="ts">
import { dictionaryServices } from '@/constants';
import { PropType } from 'vue';

interface CustomAction {
    name: string;
    link: string;
}

defineProps({
    word: String,
    customActions: Array as PropType<CustomAction[]>,
})
</script>