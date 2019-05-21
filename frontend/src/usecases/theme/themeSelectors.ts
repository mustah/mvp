import {createSelector} from 'reselect';
import {makeTheme, Theme, withStyles} from '../../app/themes';
import {RootState} from '../../reducers/rootReducer';
import {ThemeState} from './themeModels';

const getThemeState = (rootState: RootState): ThemeState => rootState.theme;

export const getTheme = createSelector<RootState, ThemeState, Theme>(
  getThemeState,
  ({color: {primary, secondary}}) => makeTheme(withStyles({primary, secondary}))
);
