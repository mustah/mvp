import {CHANGE_LANGUAGE} from '../languageActions';
import {LanguageState} from '../languageModels';
import {language} from '../languageReducer';

describe('languageReducer', () => {
  it('has initialState swedish', () => {
    const initialState: LanguageState = language(undefined, {type: 'unknown'});

    const expected: LanguageState = {language: 'sv'};
    expect(initialState).toEqual(expected);
  });

  it('sets language', () => {
    const initialState: LanguageState = language(undefined, {type: 'unknown'});

    const expected: LanguageState = {language: 'en'};
    expect(language(initialState, {type: CHANGE_LANGUAGE, payload: 'en'})).toEqual(expected);
  });
});
