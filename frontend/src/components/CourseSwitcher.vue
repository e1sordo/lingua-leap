<template>
    <div class="dropdown">
        <button v-if="activeCourse" class="btn btn-outline-light dropdown-toggle" data-bs-toggle="dropdown"
            type="button" id="courseSwitcherDropdown">
            {{ activeCourse.targetLanguageName }}
            <span v-if="activeCourse.dueToday > 0" class="badge text-bg-danger">
                {{ activeCourse.dueToday }}
            </span>
        </button>
        <ul class="dropdown-menu" aria-labelledby="courseSwitcherDropdown">
            <li v-for="course in courses" :key="course.id">
                <a class="dropdown-item" href="#" :class="{ active: course.id === activeCourseId }"
                    @click.prevent="handleSelect(course.id)">
                    {{ course.targetLanguageName }}
                    <small class="text-muted ps-1">
                        {{ course.translationLanguageCodes.join(', ') }}
                    </small>
                    <span v-if="course.dueToday > 0" class="badge text-bg-danger ms-1">
                        {{ course.dueToday }}
                    </span>
                </a>
            </li>
            <li>
                <hr class="dropdown-divider" />
            </li>
            <li>
                <a class="dropdown-item" href="#" data-bs-toggle="modal" data-bs-target="#courseCreateModal">
                    {{ $t("course.addLanguage") }}
                </a>
            </li>
        </ul>
    </div>
</template>

<script setup lang="ts">
import { useCourses } from '@/composables/useCourses';

const { courses, activeCourseId, activeCourse, selectCourse } = useCourses();

const handleSelect = (id: number) => {
    selectCourse(id);
};
</script>
