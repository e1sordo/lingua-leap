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
            <li>
                <a class="dropdown-item" :href="`https://www.spanishdict.com/translate/${word}`" target="_blank">
                    Span¡shD!ctionary
                    <i class="bi bi-box-arrow-up-right"></i>
                </a>
            </li>
            <li>
                <a class="dropdown-item" :href="`https://youglish.com/pronounce/${word}/spanish`" target="_blank">
                    YouGlish
                    <i class="bi bi-box-arrow-up-right"></i>
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
defineProps({ word: String })
</script>