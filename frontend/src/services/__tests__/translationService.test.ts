import {capitalized} from '../translationService';

describe('translationService', () => {

  describe('capitalized', () => {

    it('empty string is fine', () => {
      expect(capitalized('')).toEqual('');
    });

    it('single word is fine', () => {
      expect(capitalized('woop')).toEqual('Woop');
    });

    it('multiple words are fine', () => {
      expect(capitalized('shoop da woop')).toEqual('Shoop Da Woop');
    });

    it('multiple words separated by multiple spaces are fine', () => {
      expect(capitalized('shoop     da    woop')).toEqual('Shoop     Da    Woop');
    });
  });
});
