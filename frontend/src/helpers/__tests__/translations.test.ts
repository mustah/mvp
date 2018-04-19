import {initTranslations} from '../../i18n/__tests__/i18nMock';
import {ParameterName} from '../../state/user-selection/userSelectionModels';
import {getTranslationOrName, statusTranslation, translatedErrorMessage} from '../translations';

describe('translations', () => {

  describe('in english', () => {

    beforeEach(() => initTranslations({
        code: 'en',
        translation: {
          'token missing or invalid': 'token must be missing',
          'bad credentials': 'no password',
          'ok': 'ok status',
          'warning': 'not good',
          'unknown': 'do not know this',
        },
      },
    ));

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

      it('does not translate parameter name period', () => {
        expect(getTranslationOrName('unknown', ParameterName.period)).toEqual('unknown');
      });

      it('translates gateway statuses', () => {
        expect(getTranslationOrName('warning', ParameterName.gatewayStatuses)).toEqual('not good');
      });

      it('translates meter statuses', () => {
        expect(getTranslationOrName('ok', ParameterName.meterStatuses)).toEqual('ok status');
      });

      it('does not translate city name when not unknown', () => {
        expect(getTranslationOrName('kungsbacka', ParameterName.cities)).toEqual('kungsbacka');
        expect(getTranslationOrName('kabelgatan 1', ParameterName.addresses)).toEqual('kabelgatan 1');
      });

    });

    describe('statusTranslation', () => {

      it('fallbacks to unknown status translations', () => {
        expect(statusTranslation('no')).toEqual('do not know this');
      });
    });

  });

  describe('translates unknown city and address', () => {

    beforeEach(() => initTranslations({
        code: 'sv',
        translation: {
          'unknown': 'okänd',
          'kungsbacka': 'should not be translated',
          'kabelgatan 1': 'should not be translated',
        },
      },
    ));

    it('translates unknown text', () => {
      expect(getTranslationOrName('unknown', ParameterName.cities)).toEqual('okänd');
      expect(getTranslationOrName('unknown', ParameterName.addresses)).toEqual('okänd');
    });

    it('just returns the input text, even when translations key is defined', () => {
      expect(getTranslationOrName('kungsbacka', ParameterName.cities)).toEqual('kungsbacka');
      expect(getTranslationOrName('kabelgatan 1', ParameterName.addresses)).toEqual('kabelgatan 1');
    });

    it('just return the input text', () => {
      expect(getTranslationOrName('stockholm', ParameterName.cities)).toEqual('stockholm');
      expect(getTranslationOrName('drottninggatan 1', ParameterName.addresses)).toEqual('drottninggatan 1');
    });
  });

});
