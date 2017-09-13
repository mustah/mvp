import {makeToken} from '../authService';

describe('authService', () => {

  describe('make new basic encoded authorization token', () => {

    it('can create new token from empty username and password', () => {
      expect(makeToken('', '')).toEqual('Og==');
    });
  });
});
