import Vue from 'vue'
import App from './App'
import Router from 'vue-router'
import Connect from './Connect.vue'
import Create from './Create'
import Root from './Root'
import Room from './Room'
import Draw from './Draw'
import s from './socketManager'

Vue.use(Router);

const routes = [
    {path:'/',component:Connect},
    {path:'/create',component:Create},
    {path:'/room',component:Room},
    {path:'/draw',component:Draw}
]
const router = new Router({
    routes
})

s.router=router;
// console.log(s)

/* eslint-disable no-new */
s.rootVue = new Vue({
  el: '#app',
  router:router,
  template: '<Root/>',
  components: { Root }
})
console.log(s.rootVue)

