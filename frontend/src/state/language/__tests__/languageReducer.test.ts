import {CHANGE_LANGUAGE} from '../languageActions';
import {LanguageState} from '../languageModels';
import {language} from '../languageReducer';

describe('languageReducer', () => {
  it('has initialState english', () => {
    const initialState: LanguageState = language(undefined, {type: 'unknown'});

    const expected: LanguageState = {language: {code: 'en'}};
    expect(initialState).toEqual(expected);
  });

  it('sets language', () => {
    const initialState: LanguageState = language(undefined, {type: 'unknown'});

    const expected: LanguageState = {language: {code: 'sv'}};
    expect(language(initialState, {type: CHANGE_LANGUAGE, payload: 'sv'})).toEqual(expected);
  });
});
