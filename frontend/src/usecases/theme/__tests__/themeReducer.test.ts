import {changePrimaryColor, changeSecondaryColor, resetColors} from '../themeActions';
import {initialState, theme, ThemeState} from '../themeReducer';

describe('themeReducer', () => {

  describe('change colors', () => {

    it('changes primary color', () => {
      const state: ThemeState = theme(initialState, changePrimaryColor('red'));

      const expected: ThemeState = {
        ...initialState,
        color: {
          ...initialState.color,
          primary: 'red'
        }
      };
      expect(state).toEqual(expected);
    });

    it('changes secondary color', () => {
      const state: ThemeState = theme(initialState, changeSecondaryColor('blue'));

      const expected: ThemeState = {
        ...initialState,
        color: {
          ...initialState.color,
          secondary: 'blue'
        }
      };
      expect(state).toEqual(expected);
    });

    it('changes both colors', () => {
      let state: ThemeState = theme(initialState, changePrimaryColor('red'));
      state = theme(state, changeSecondaryColor('blue'));

      const expected: ThemeState = {
        ...initialState,
        color: {
          primary: 'red',
          secondary: 'blue'
        }
      };
      expect(state).toEqual(expected);
    });

    it('reset colors', () => {
      let state: ThemeState = {
        color: {
          primary: 'red',
          secondary: 'blue'
        }
      };
      state = theme(state, resetColors());

      expect(state).toBe(initialState);
    });
  });
});
