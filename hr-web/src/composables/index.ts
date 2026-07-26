import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 组合式函数：监听元素进入视口时触发动画
 * 返回 [ref, boolean]，第二个值表示是否可见
 */
export function useInView(threshold = 0.1) {
  const target = ref<HTMLElement | null>(null)
  const isInView = ref(false)

  onMounted(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          isInView.value = true
          observer.unobserve(entry.target)
        }
      },
      { threshold },
    )
    if (target.value) observer.observe(target.value)
  })

  onUnmounted(() => {
    // cleanup
  })

  return { target, isInView }
}

/**
 * 打字机效果
 */
export function useTypewriter(words: string[], speed = 100, pause = 2000) {
  const currentText = ref('')
  const wordIndex = ref(0)
  const charIndex = ref(0)
  const isDeleting = ref(false)

  function type() {
    const currentWord = words[wordIndex.value % words.length]

    if (!isDeleting.value) {
      currentText.value = currentWord.substring(0, charIndex.value + 1)
      charIndex.value++
      if (charIndex.value === currentWord.length) {
        isDeleting.value = true
        setTimeout(type, pause)
        return
      }
    } else {
      currentText.value = currentWord.substring(0, charIndex.value - 1)
      charIndex.value--
      if (charIndex.value === 0) {
        isDeleting.value = false
        wordIndex.value++
      }
    }

    const delay = isDeleting.value ? speed / 2 : speed
    setTimeout(type, delay)
  }

  onMounted(() => {
    type()
  })

  return currentText
}

/**
 * 滚动位置监听
 */
export function useScrollPosition() {
  const scrollY = ref(0)

  onMounted(() => {
    const handleScroll = () => { scrollY.value = window.scrollY }
    window.addEventListener('scroll', handleScroll, { passive: true })
    onUnmounted(() => window.removeEventListener('scroll', handleScroll))
  })

  return scrollY
}
