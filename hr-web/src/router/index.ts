import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('../views/front/FrontLayout.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('../views/front/HomeView.vue'),
        },
      ],
    },
    {
      path: '/admin',
      redirect: '/admin/',
      children: [
        {
          path: '',
          redirect: 'https://localhost:5174',
        },
      ],
    },
  ],
})

export default router
