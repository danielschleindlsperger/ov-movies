ready(() => {
  const md = '(min-width: 768px)'
  const media = window.matchMedia(md)
  const toggleButton = document.querySelector('[data-mobile-form-toggle]')
  const form = document.querySelector('[data-filter-form]')

  let isOpen = media.matches

  if (!toggleButton) {
    throw new Error('Toggle Button not found.')
  }

  const update = () => {
    if (isOpen) {
      form.classList.remove('hidden')
      toggleButton.classList.add('rotate-180')
    } else {
      form.classList.add('hidden')
      toggleButton.classList.remove('rotate-180')
    }
  }

  toggleButton.addEventListener('click', () => {
    // toggle form display
    isOpen = !isOpen
    update()
  })

  // update form display when viewport becomes smaller/larger
  media.addEventListener('change', ({ matches }) => {
    isOpen = matches
    update()
  })

  // run once initially
  update()
})

function ready(fn) {
  if (document.readyState != 'loading') {
    fn()
  } else {
    document.addEventListener('DOMContentLoaded', fn)
  }
}
