import {TokenService} from '../TokenService';
import {FakeStorage} from './FakeStorage';

describe('TokenService', () => {

  let tokenService: TokenService;

  beforeEach(() => {
    tokenService = new TokenService(new FakeStorage());
  });

  describe('empty localStorage', () => {

    it('should not have a token ', () => {
      expect(tokenService.getToken()).toBeUndefined();
    });

    it('should not set null token', () => {
      tokenService.setToken(null);

      expect(tokenService.getToken()).toBeUndefined();
    });
  });

  describe('make new encoded authorization token', () => {

    it('can create new token from empty username and password', () => {
      expect(TokenService.makeToken('', '')).toEqual('Og==');
    });
  });

  describe('localStorage with token', () => {

    it('should have token', () => {
      tokenService.setToken('fooboo');

      expect(tokenService.getToken()).toEqual('fooboo');
    });

    it('should replace previously stored token', () => {
      tokenService.setToken('foo');

      expect(tokenService.getToken()).toEqual('foo');

      tokenService.setToken('boo');

      expect(tokenService.getToken()).toEqual('boo');
    });

    it('should clear stored token', () => {
      tokenService.setToken('foo');

      tokenService.clear();

      expect(tokenService.getToken()).toBeUndefined();
    });

    it('can clear empty storage', () => {
      tokenService.clear();

      expect(tokenService.getToken()).toBeUndefined();
    });
  });

});
