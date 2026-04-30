import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import WorkflowView from '../views/WorkflowView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/workflow',
      name: 'workflow',
      component: WorkflowView,
    },
  ],
})

export default router
