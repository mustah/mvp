import {InvalidToken} from '../../exceptions/InvalidToken';
import {EndPoints} from '../endPoints';
import {authenticate, restClient, restClientWith, wasRequestCanceled} from '../restClient';
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
        mockRestClient.onGet(EndPoints.selectionTree).reply(401, {message: 'Token missing or invalid'});
        return restClient.get(EndPoints.selectionTree);
      };

      const error = new InvalidToken('Token missing or invalid');
      await expect(getInvalidTokenError()).rejects.toEqual(error);
    });

    it('doesnt throws a InvalidToken exception for errors not related to token invalid', async () => {

      const getErrorElseThanInvalidToken = async () => {
        mockRestClient.onGet(EndPoints.selectionTree).reply(401, {message: 'An other error'});
        return restClient.get(EndPoints.selectionTree);
      };

      const error = new InvalidToken('Token missing or invalid');
      await expect(getErrorElseThanInvalidToken()).rejects.not.toEqual(error);
    });
  });

  describe('cancel request', () => {
    let mockRestClient;
    beforeEach(() => {
      restClientWith('123123123');
      mockRestClient = new MockAdapter(restClient, {delayResponse: 200});
    });

    const getRequestFromURL = async (url: string) => {
      mockRestClient.onGet(url).reply(201, 'some data');
      try {
        return await restClient.get(url);
      } catch (error) {
        return Promise.resolve(error);
      }
    };

    it('cancels all but the last of simultaneous requests to the same endpoint', async () => {
      const url = '/endPoint';
      const response = await Promise.all([getRequestFromURL(url), getRequestFromURL(url)]);
      expect(wasRequestCanceled(response[0])).toBeTruthy();
      expect(response[1].data).toEqual('some data');
    });

    it('cancel requests for same endpoint even if parameters differ', async () => {
      const url1 = '/endPoint?city=abc';
      const url2 = '/endPoint?id=5';
      const response = await Promise.all([getRequestFromURL(url1), getRequestFromURL(url2)]);
      expect(wasRequestCanceled(response[0])).toBeTruthy();
      expect(response[1].data).toEqual('some data');
    });

    it('handles more than two simultaneous requests to the same endpoint', async () => {
      const url1 = '/endPoint?city=abc';
      const url2 = '/endPoint?id=5';
      const url3 = '/endPoint?id=5&address=kungsgatan';
      const response = await Promise.all([getRequestFromURL(url1), getRequestFromURL(url2), getRequestFromURL(url3)]);
      expect(wasRequestCanceled(response[0])).toBeTruthy();
      expect(wasRequestCanceled(response[1])).toBeTruthy();
      expect(response[2].data).toEqual('some data');
    });

    it('resolves requests to different endPoints', async () => {
      const url1 = '/firstEndpoint';
      const url2 = '/secondEndpoint';
      const response = await Promise.all([getRequestFromURL(url1), getRequestFromURL(url2)]);
      expect(response[0].data).toEqual('some data');
      expect(response[1].data).toEqual('some data');
    });
  });
});
