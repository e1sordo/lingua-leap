import App from '@/App.vue';
import i18n from '@/i18n';
import '@/logout-scheduler';
import router from '@/router';
import { listenForThemeChanges } from '@/theme';
import 'bootstrap-icons/font/bootstrap-icons.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import { createApp } from 'vue';

listenForThemeChanges();

createApp(App)
    .use(router)
    .use(i18n as any)
    .mount('#app')
