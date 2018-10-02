import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {getLanguages} from '../languageSelectors';

describe('languageSelectors', () => {

  it('returns supported languages in english', () => {
    initTranslations({
      code: 'en',
      translation: {
        swedish: 'swedish',
        english: 'english',
      },
    });

    expect(getLanguages()).toEqual([
      {code: 'sv', name: 'Swedish'},
      {code: 'en', name: 'English'},
    ]);
  });

  it('returns supported languages in swedish', () => {
    initTranslations({
      code: 'sv',
      translation: {
        swedish: 'svenska',
        english: 'engelska',
      },
    });

    expect(getLanguages()).toEqual([
      {code: 'sv', name: 'Svenska'},
      {code: 'en', name: 'Engelska'},
    ]);
  });
});
