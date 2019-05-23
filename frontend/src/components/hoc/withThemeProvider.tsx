import {object} from 'prop-types';
import {compose, getContext, withContext} from 'recompose';
import {CssStyles} from '../../app/colors';

const contextType = {cssStyles: object.isRequired};

const contextEnhancer = withContext(contextType, ({cssStyles}) => ({cssStyles}));

export interface ThemeContext {
  cssStyles: CssStyles;
}

export const withThemeProvider = compose<ThemeContext, ThemeContext>(contextEnhancer);

export const withCssStyles = getContext(contextType);
