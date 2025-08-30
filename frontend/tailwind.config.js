/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './solsol/index.html',
    './solsol/src/**/*.{vue,js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      fontFamily: {
        brand: ['"Noto Sans KR"', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}


