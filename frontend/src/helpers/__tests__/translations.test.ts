import {initTranslations} from '../../i18n/__tests__/i18nMock';
import {translate} from '../../services/translationService';
import {ParameterName} from '../../state/user-selection/userSelectionModels';
import {getTranslationOrName, orUnknown, statusTranslation, translatedErrorMessage} from '../translations';

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

    it('fallbacks to unknown when there is no translated name', () => {
      expect(orUnknown(undefined)).toEqual('okänd');
    });

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

  describe('translations of backend provided units', () => {

    it('translates units', () => {
      // by adding these to the source code, they are included in the POT file
      // and thus required in the PO files for each language
      translate('Volume short');
      translate('Flow short');
      translate('Energy short');
      translate('Power short');
      translate('Forward temperature short');
      translate('Return temperature short');
      translate('Difference temperature short');
      translate('External temperature short');
      translate('Temperature short');
      translate('Relative humidity short');
      translate('Energy return short');
      translate('Reactive energy short');
      translate('close');
      translate('yes');
      translate('no');
      translate('alarm', {count: 2});
    });

    it('translates organisation asset types', () => {
      translate('logotype');
      translate('login_logotype');
      translate('login_background');
    });

  });

});
