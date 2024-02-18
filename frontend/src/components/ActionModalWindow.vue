<template>
    <div class="modal fade" :id="id" tabindex="-1" :aria-labelledby="id + 'Label'" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" :id="id + 'Label'">{{ title }}</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" />
                </div>
                <slot></slot>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps({
    id: String,
    title: String
})
</script>