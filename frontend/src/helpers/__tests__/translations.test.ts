import {initTranslations} from '../../i18n/__tests__/i18n-testdata';
import {ParameterName} from '../../state/search/selection/selectionModels';
import {getTranslationOrName, statusTranslation, translatedErrorMessage} from '../translations';

describe('translations', () => {

  initTranslations({
      code: 'en',
      translation: {
        'token missing or invalid': 'token must be missing',
        'bad credentials': 'no password',
        'ok': 'ok status',
        'warning': 'not good',
        'unknown': 'do not know this',
      },
    },
  );

  describe('translateErrorMessage', () => {

    it('does not translate the message', () => {
      expect(translatedErrorMessage('some unknown error')).toBe('some unknown error');
    });

    describe('translates to english', () => {

      it('translates message to english', () => {
        expect(translatedErrorMessage('Token missing or invalid')).toEqual('Token must be missing');
        expect(translatedErrorMessage('Bad credentials')).toEqual('No password');
      });
    });
  });

  describe('getTranslationOrName', () => {

    it('does not translate unknown status', () => {
      expect(getTranslationOrName('unknown', ParameterName.addresses)).toEqual('unknown');
    });

    it('translates gateway statuses', () => {
      expect(getTranslationOrName('warning', ParameterName.gatewayStatuses)).toEqual('not good');
    });

    it('translates meter statuses', () => {
      expect(getTranslationOrName('ok', ParameterName.meterStatuses)).toEqual('ok status');
    });
  });

  describe('statusTranslation', () => {

    it('fallbacks to unknown status translations', () => {
      expect(statusTranslation('no')).toEqual('do not know this');
    });
  });
});
