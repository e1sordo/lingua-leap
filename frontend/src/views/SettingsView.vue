<template>
  <div class="home">
    <!-- <div class="m-4 text-start">
      <h2>{{ $t("settings.tasks.title") }}</h2>
      <task-table />
    </div>

    <hr /> -->

    <div class="m-4 text-start">
      <h2>{{ $t("settings.language.title") }}</h2>
      <LocaleSwitcher />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import LocaleSwitcher from '@/components/LocaleSwitcher.vue';

// interface State {
//   msg: string;
//   backendResponse: string;
//   errors: AxiosError[]
// }

const jiraSettignsKey = "settings:jira";

export default defineComponent({
  name: 'SettingsView',
  components: {
    // TaskTable, 
    LocaleSwitcher
  },

  // data: (): State => {
  //   return {
  //     msg: 'HowTo call REST-Services:',
  //     backendResponse: '',
  //     errors: []
  //   }
  // },

  data() {
    return {
      proxyUrl: '',
      serverUrl: '',
      username: '',
      password: '',
    };
  },
  created() {
    this.loadJiraSettings();
  },

  methods: {
    // Fetches posts when the component is created.
    // callHelloApi() {
    //   api.weeks().then(response => {
    //     // this.backendResponse = response.data;
    //     console.log(response.data)
    //   })
    //     .catch((error: AxiosError) => {
    //       this.errors.push(error)
    //     })
    // },
    loadJiraSettings() {
      const savedSettings = localStorage.getItem(jiraSettignsKey);
      if (savedSettings) {
        const { proxyUrl, serverUrl, username, password } = JSON.parse(savedSettings);
        this.proxyUrl = proxyUrl;
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
      }
    },
    saveJiraSettings() {
      const settings = {
        proxyUrl: this.proxyUrl,
        serverUrl: this.serverUrl,
        username: this.username,
        password: this.password,
      };
      localStorage.setItem(jiraSettignsKey, JSON.stringify(settings));
      alert('Настройки сохранены!');
    }
  }
});
</script>