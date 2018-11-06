module.exports = function(wallaby) {
  return {
    files: [
      'package.json',
      'tsconfig.json',
      'src/**/*.js',
      'src/**/*.ts',
      '!src/**/__tests__/*.test.ts',
    ],

    tests: ['src/**/__tests__/*.test.ts'],

    env: {
      type: 'node',
    },

    testFramework: 'jest',

    setup: function(wallaby) {
      const jestConfig = require('./package.json').jest;
      delete jestConfig.transform;
      wallaby.testFramework.configure(jestConfig);
    },

    compilers: {
      'src/**/*.ts': wallaby.compilers.typeScript({
        module: 'commonjs',
        jsx: 'react',
        target: 'es5',
      }),
    },
  };
};
