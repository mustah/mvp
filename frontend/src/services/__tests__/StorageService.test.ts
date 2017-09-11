import {StorageService} from '../StorageService';
import {FakeStorage} from './FakeStorage';

describe('StorageService', () => {

  const key = 'myKey';
  let storageService: StorageService;

  beforeEach(() => {
    storageService = new StorageService(new FakeStorage());
  });

  describe('empty localStorage', () => {

    it('should not have a item ', () => {
      expect(storageService.getItem('test')).toBeUndefined();
    });
  });

  describe('localStorage with data', () => {

    it('should have token', () => {
      storageService.setItem('fooKey', 'fooboo');

      expect(storageService.getItem('fooKey')).toEqual('fooboo');
    });

    it('should replace previously stored data', () => {
      storageService.setItem(key, 'foo');

      expect(storageService.getItem(key)).toEqual('foo');

      storageService.setItem(key, 'boo');

      expect(storageService.getItem(key)).toEqual('boo');
    });

    it('should clear all stored data', () => {
      storageService.setItem(key, 'foo');

      storageService.clear();

      expect(storageService.getItem(key)).toBeUndefined();
    });

    it('can clear empty storage', () => {
      storageService.clear();

      expect(storageService.getItem(key)).toBeUndefined();
    });

    it('removes stored data by key', () => {
      storageService.setItem(key, 'clark');

      storageService.removeItem(key);

      expect(storageService.getItem(key)).toBeUndefined();
    });
  });

});
