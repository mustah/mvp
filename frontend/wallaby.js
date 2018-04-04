module.exports = function(wallaby) {
  return {
    files: [
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

    compilers: {
      'src/**/*.ts': wallaby.compilers.typeScript({
        module: 'commonjs',
        jsx: 'react',
        target: 'es5',
      }),
    },
  };
};
