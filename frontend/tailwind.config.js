/** @type {import('tailwindcss').Config} */
module.exports = {
  // tell Tailwind to look inside all Angular components
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      // add custom colors
      colors: {
        'bank-blue': '#1E3A8A',    // A deep, trustworthy blue
        'bank-rose': '#F43F5E',    // A modern, vibrant rose for actions
        'bank-light': '#F8FAFC',   // A light, clean background color
      }
    },
  },
  plugins: [],
}

