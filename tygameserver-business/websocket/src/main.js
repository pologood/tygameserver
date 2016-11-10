import Vue from 'vue'
import App from './App'
import Router from 'vue-router'
import Connect from './Connect.vue'
import Create from './Create'
import Root from './Root'

Vue.use(Router);

const routes = [
    {path:'/',component:Connect},
    {path:'/create',component:Create}
]
const router = new Router({
    routes
})

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router:router,
  template: '<Root/>',
  components: { Root }
})

