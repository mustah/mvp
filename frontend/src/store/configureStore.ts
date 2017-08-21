export const configureStore = process.env.NODE_ENV === 'production'
  ? require.resolve('./configureStore.prod')
  : require.resolve('./configureStore.dev');
