module.exports = [
  {
    ignores: ["node_modules/**", "coverage/**", "tests/**"]
  },
  {
    files: ["src/**/*.js"],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: "commonjs",
      globals: {
        module: "readonly",
        require: "readonly",
        document: "readonly",
        fetch: "readonly",
        window: "readonly",
        globalThis: "readonly",
        setTimeout: "readonly",
        clearTimeout: "readonly",
        console: "readonly",
        test: "readonly",
        expect: "readonly",
        describe: "readonly",
        beforeEach: "readonly",
        jest: "readonly"
      }
    },
    rules: {}
  }
];