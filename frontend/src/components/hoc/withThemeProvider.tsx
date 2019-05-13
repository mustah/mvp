import {object} from 'prop-types';
import {compose, getContext, withContext} from 'recompose';
import {CssStyles} from '../../app/colors';

export const ThemeContextTypes = {
  cssStyles: object,
};

export interface ThemeContext {
  cssStyles: CssStyles;
}

const contextEnhancer = withContext<ThemeContext, ThemeContext>(ThemeContextTypes, ({cssStyles}) => ({cssStyles}));

export const withThemeProvider = compose<ThemeContext, ThemeContext>(contextEnhancer);

export const withCssStyles = getContext(ThemeContextTypes);
