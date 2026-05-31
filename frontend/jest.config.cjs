module.exports = {
  testEnvironment: "jsdom",
  collectCoverageFrom: ["src/**/*.{js,jsx,ts,tsx}", "!src/index.{js,jsx}"],
  coverageDirectory: "coverage",
  testMatch: ["**/src/__tests__/**/*.test.js", "**/src/__tests__/**/*.test.jsx"]
};