import {EndPoints} from '../../state/domain-models/domainModels';
import {authenticate, InvalidToken, restClient, restClientWith} from '../restClient';
import MockAdapter = require('axios-mock-adapter');

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

  describe('token invalid', () => {
    let mockRestClient;

    beforeEach(() => {
      restClientWith('123123123');
      mockRestClient = new MockAdapter(restClient);
    });

    it('throws a InvalidToken exception if token invalid from backend', async () => {

      const getInvalidTokenError = async () => {
        mockRestClient.onGet(EndPoints.allMeters).reply(401, {message: 'Token missing or invalid'});
        return restClient.get(EndPoints.allMeters);
      };

      const error = new InvalidToken('Token missing or invalid');
      await expect(getInvalidTokenError()).rejects.toEqual(error);
    });

    it('doesnt throws a InvalidToken exception for errors not related to token invalid', async () => {

      const getErrorElseThanInvalidToken = async () => {
        mockRestClient.onGet(EndPoints.allMeters).reply(401, {message: 'An other error'});
        return restClient.get(EndPoints.allMeters);
      };

      const error = new InvalidToken('Token missing or invalid');
      await expect(getErrorElseThanInvalidToken()).rejects.not.toEqual(error);
    });
  });
});
