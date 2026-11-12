import api, { CourseDto, CreateCourseRequestDto, LanguageDto } from '@/api/backend-api';
import { computed, reactive } from 'vue';

const ACTIVE_COURSE_STORAGE_KEY = 'lingualeap:activeCourseId';

interface CoursesState {
    courses: CourseDto[];
    languages: LanguageDto[];
    languagesLoaded: boolean;
    activeCourseId: number | null;
    loaded: boolean;
    loading: Promise<void> | null;
    error: string | null;
}

const state = reactive<CoursesState>({
    courses: [],
    languages: [],
    languagesLoaded: false,
    activeCourseId: null,
    loaded: false,
    loading: null,
    error: null
});

const activeCourse = computed(() =>
    state.courses.find(course => course.id === state.activeCourseId) || null
);

const hasCourses = computed(() => state.courses.length > 0);

function pickInitialCourseId(): number | null {
    if (state.courses.length === 0) {
        return null;
    }

    const savedId = Number(localStorage.getItem(ACTIVE_COURSE_STORAGE_KEY));
    if (Number.isInteger(savedId) && state.courses.some(course => course.id === savedId)) {
        return savedId;
    }

    return state.courses[0].id;
}

async function loadCourses(): Promise<void> {
    if (state.loaded) {
        return;
    }
    if (state.loading) {
        return state.loading;
    }

    state.loading = (async () => {
        try {
            state.error = null;
            const response = await api.getCourses();
            state.courses = response.data;

            if (state.activeCourseId === null
                || !state.courses.some(course => course.id === state.activeCourseId)) {
                state.activeCourseId = pickInitialCourseId();
            }
        } catch (error) {
            console.error(error);
            state.error = 'Failed to load courses';
        } finally {
            state.loaded = true;
            state.loading = null;
        }
    })();

    return state.loading;
}

function selectCourse(id: number): boolean {
    if (!state.courses.some(course => course.id === id)) {
        return false;
    }

    state.activeCourseId = id;
    localStorage.setItem(ACTIVE_COURSE_STORAGE_KEY, String(id));
    return true;
}

async function createCourse(request: CreateCourseRequestDto): Promise<CourseDto> {
    const response = await api.createCourse(request);
    const course = response.data;

    state.courses.push(course);
    selectCourse(course.id);

    return course;
}

async function refreshCourses(): Promise<void> {
    try {
        state.error = null;
        const response = await api.getCourses();
        const previousActiveId = state.activeCourseId;
        state.courses = response.data;

        if (previousActiveId !== null
            && state.courses.some(course => course.id === previousActiveId)) {
            state.activeCourseId = previousActiveId;
        } else {
            state.activeCourseId = pickInitialCourseId();
        }
    } catch (error) {
        console.error(error);
        state.error = 'Failed to refresh courses';
    }
}

async function loadLanguages(): Promise<void> {
    if (state.languagesLoaded) {
        return;
    }

    try {
        const response = await api.getLanguages();
        state.languages = response.data;
    } catch (error) {
        console.error(error);
        state.error = 'Failed to load languages';
    } finally {
        state.languagesLoaded = true;
    }
}

function updateActiveCourseCounts(update: (course: CourseDto) => Partial<CourseDto>): void {
    const course = state.courses.find(item => item.id === state.activeCourseId);
    if (course) {
        Object.assign(course, update(course));
    }
}

export function useCourses() {
    return {
        state,
        courses: computed(() => state.courses),
        languages: computed(() => state.languages),
        activeCourseId: computed(() => state.activeCourseId),
        activeCourse,
        hasCourses,
        loadCourses,
        loadLanguages,
        selectCourse,
        createCourse,
        refreshCourses,
        updateActiveCourseCounts
    };
}
