import FeedView from '@/views/FeedView.vue';
import StudyView from '@/views/StudyView.vue';
import StudyAsTableView from '@/views/study/StudyAsTableView.vue';
import StudyNumbersView from '@/views/study/StudyNumbersView.vue';
import ListenAbbreviationsView from '@/views/study/ListenAbbreviationsView.vue';
import ListsView from '@/views/ListsView.vue';
import ListDetailView from '@/views/ListDetailView.vue';
import SettingsView from '@/views/SettingsView.vue';
import WordDetailView from '@/views/WordDetailView.vue';
import WordFormView from '@/views/WordFormView.vue';
import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    name: 'home',
    meta: { title: 'LinguaLeap' },
    component: FeedView
  },
  {
    path: '/study',
    name: 'study',
    meta: { title: 'Study' },
    component: StudyView
  },
  {
    path: '/study/table',
    name: 'studyTable',
    meta: { title: 'Table' },
    component: StudyAsTableView
  },
  {
    path: '/study/numbers',
    name: 'studyNumbers',
    meta: { title: 'Numbers' },
    component: StudyNumbersView
  },
  {
    path: '/study/abbreviations',
    name: 'studyAbbreviations',
    meta: { title: 'Abbreviations' },
    component: ListenAbbreviationsView
  },
  {
    path: '/lists',
    name: 'lists',
    meta: { title: 'My Lists' },
    component: ListsView
  },
  {
    path: '/lists/:id',
    name: 'listDetail',
    meta: { title: 'List' },
    component: ListDetailView
  },
  {
    path: '/add',
    name: 'addNewWord',
    meta: { title: 'Add' },
    component: WordFormView
  },
  {
    path: '/words/:word',
    name: 'wordDetail',
    meta: { title: 'Detail' },
    component: WordDetailView
  },
  {
    path: '/settings',
    name: 'settings',
    meta: { title: 'Settings' },
    component: SettingsView
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

router.beforeEach((to, from, next) => {
  document.title = to.meta.title as string;
  next();
});

export default router;
