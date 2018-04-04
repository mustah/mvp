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

    it('does not translate un mapped status', () => {
      const status = {id: 'x', name: 'unknown'};

      expect(getTranslationOrName(status, ParameterName.addresses)).toEqual('unknown');
    });

    it('translates gateway statuses', () => {
      const status = {id: 'warning', name: 'ok'};

      expect(getTranslationOrName(status, ParameterName.gatewayStatuses)).toEqual('not good');
    });
  });

  describe('statusTranslation', () => {

    it('fallbacks to unknown status translations', () => {
      expect(statusTranslation({id: 'no', name: 'x'})).toEqual('do not know this');
    });
  });
});
