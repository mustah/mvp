'use strict';

module.exports = function(wallaby) {

  wallaby.defaults.files.instrument = false;

  return {
    files: [
      'tsconfig.json',
      'src/**/*.ts',
      '!src/**/__tests__/*.test.ts',
    ],

    tests: ['src/**/__tests__/*.test.ts'],

    env: {
      type: 'node',
      runner: 'node',
    },

    compilers: {
      '**/*.ts': wallaby.compilers.typeScript({}),
    },

    testFramework: 'jest',

    setup: function(wallaby) {
      const jestConfig = require('./package.json').jest;
      jestConfig.globals = {'__DEV__': true};
      wallaby.testFramework.configure(jestConfig);
    },

  };
};
