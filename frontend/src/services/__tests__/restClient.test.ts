import {authenticate, restClient, restClientWith} from '../restClient';

describe('restClient', () => {

  describe('restClientWith', () => {

    it('does not create any new restClient instance when no token exists', () => {
      restClientWith(undefined);

      expect(restClient.defaults.headers.Authorization).toBeUndefined();
    });

    it('creates restClient with bearer authentication header', () => {
      restClientWith('123-1234');

      expect(restClient.defaults.headers.Authorization).toBe('Bearer 123-1234');
    });

    it('creates restClient with basic authentication header', () => {
      authenticate('81818-19911');

      expect(restClient.defaults.headers.Authorization).toBe('Basic 81818-19911');
    });
  });
});
