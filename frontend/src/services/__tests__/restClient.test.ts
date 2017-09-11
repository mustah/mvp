import {initRestClient, restClient} from '../restClient';

describe('restClient', () => {

  describe('initRestClient', () => {

    it('does not create any new restClient instance when no token exists', () => {
      initRestClient(undefined);

      expect(restClient.defaults.headers.Authorization).toBeUndefined();
    });

    it('creates restClient with Authorization header set', () => {
      initRestClient('token');

      expect(restClient.defaults.headers.Authorization).toBe('Basic token');
    });
  });
});
