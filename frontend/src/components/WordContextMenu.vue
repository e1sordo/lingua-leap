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
            <li>
                <hr class="dropdown-divider" />
            </li>
            <li>
                <h6 class="dropdown-header">Lists</h6>
            </li>
            <li>
                <a class="dropdown-item" href="#">
                    Separated link
                </a>
            </li>
        </ul>
    </div>
</template>

<script setup lang="ts">
import { dictionaryServices } from '@/constants';

defineProps({ word: String })
</script>