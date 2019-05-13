import {important} from 'csx';
import {MuiTheme} from 'material-ui/styles';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import * as React from 'react';
import {style} from 'typestyle';
import {colors, CssStyles, withStyles} from './colors';

interface Styles {
  [key: string]: React.CSSProperties;
}

interface FontSize {
  small: number;
  normal: number;
  medium: number;
}

export interface Theme {
  muiTheme: MuiTheme;
  cssStyles: CssStyles;
}

export const fontFamily = 'TTNorms, Arial, sans-serif';

export const fontSize: FontSize = {
  small: 12,
  normal: 14,
  medium: 16,
};

export const borderRadius = 4;

export const boxShadow =
  '0px 1px 3px 0px rgba(0, 0, 0, 0.2), 0px 1px 1px 0px rgba(0, 0, 0, 0.14), 0px 2px 1px -1px rgba(0, 0, 0, 0.12)';

const popoverBoxShadow =
  '0px 5px 5px -3px rgba(0,0,0,0.2), 0px 8px 10px 1px rgba(0,0,0,0.14), 0px 3px 14px 2px rgba(0,0,0,0.12)';

const makeTheme = (cssStyles: CssStyles): Theme => {
  const {primary, secondary} = cssStyles;
  return ({
    cssStyles,
    muiTheme: getMuiTheme({
      badge: {
        secondaryColor: colors.notification,
        secondaryTextColor: colors.white,
      },
      checkbox: {
        checkedColor: primary.bg,
        boxColor: primary.fgHover,
      },
      dialog: {
        bodyFontSize: fontSize.normal,
        bodyColor: colors.black,
      },
      flatButton: {
        buttonFilterColor: primary.bg,
        color: colors.white,
        secondaryTextColor: secondary.fg,
        textColor: colors.white,
      },
      fontFamily,
      listItem: {
        nestedLevelDepth: 14,
      },
      menuItem: {
        hoverColor: primary.bgHover,
        selectedTextColor: primary.bg,
      },
      palette: {
        primary1Color: primary.bg,
        textColor: colors.black,
      },
      raisedButton: {
        primaryColor: primary.bg,
      },
      toggle: {
        trackOnColor: primary.bgActive,
        thumbOnColor: primary.bg,
      }
    }),
  });
};

export const mvpTheme: Theme = makeTheme(withStyles({primary: '#0091ea', secondary: '#b6e2cc'}));

export const makeGridClassName = ({primary}: CssStyles): string =>
  style({
    $nest: {
      '&.k-grid tr:hover': {backgroundColor: important(primary.bgHover)},
      '&.k-grid .k-grid-pager :hover .k-link': {backgroundColor: important(primary.bgHover)},
      '&.k-grid .k-grid-pager .k-state-selected': {backgroundColor: important(primary.bg)},
      '&.k-grid .k-grid-pager .k-link.k-pager-nav:hover': {backgroundColor: important(primary.bgHover)},
      '&.k-grid .k-grid-pager :hover .k-state-selected': {backgroundColor: important(primary.bg)},
      '&.k-grid .k-textbox': {border: `1px solid ${primary.bg}`, height: 32, fontSize: fontSize.medium},
      '&.k-grid .k-numerictextbox': {border: `1px solid ${primary.bg}`},
    },
  });

export const topMenuHeight: number = 60;

export const drawerContainerStyle: React.CSSProperties = {
  boxShadow,
  top: topMenuHeight,
  paddingBottom: topMenuHeight + 24,
};

export const sideMenuWidth = 300;

export const iconStyle: React.CSSProperties = {
  padding: 0,
  width: 30,
  height: 30,
};

export const iconSizeMedium: React.CSSProperties = {
  width: 26,
  height: 26,
};

export const actionMenuItemIconStyle: React.CSSProperties = {
  height: 18,
  width: 18,
  padding: 0,
  margin: '7px 0 0 4px',
};

export const topMenuItemIconStyle: React.CSSProperties = {
  ...actionMenuItemIconStyle,
  margin: '4px 0 0 4px',
  height: 22,
  width: 22,
};

export const dividerStyle: React.CSSProperties = {
  backgroundColor: colors.dividerColor
};

export const selectedMenuItemStyle: React.CSSProperties = {
  fontWeight: 'bold',
};

export const menuItemStyle: React.CSSProperties = {
  fontSize: fontSize.normal,
  textStyle: {
    textOverflow: 'ellipsis',
    maxWidth: 150,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
  },
};

export const listItemStyle: React.CSSProperties = {
  borderRadius: '0 50px 50px 0',
  ...menuItemStyle,
};

export const listItemStyleSelected: React.CSSProperties = {
  ...listItemStyle,
  ...selectedMenuItemStyle,
};

export const menuItemInnerDivStyle: React.CSSProperties = {
  ...menuItemStyle,
  lineHeight: '32px',
  minHeight: 32,
};

export const topMenuItemDivStyle: React.CSSProperties = {
  ...menuItemInnerDivStyle,
  fontSize: 16,
};

export const topMenuInnerDivStyle: React.CSSProperties = {
  padding: '0 0 0 38px',
};

export const popoverStyle: React.CSSProperties = {
  borderRadius,
  boxShadow: popoverBoxShadow,
};

export const dropdownStyle: Styles = {
  popoverStyle: {marginTop: 6, marginLeft: 2, ...popoverStyle},
  listStyle: {outline: 'none', paddingLeft: 5, flex: 1},
};

export const underlineFocusStyle: React.CSSProperties = {
  borderWidth: 2,
};

export const paperStyle: React.CSSProperties = {
  paddingTop: 16,
  paddingBottom: 16,
  boxShadow,
  borderRadius,
};

export const mainContentPaperStyle: React.CSSProperties = {
  ...paperStyle,
  paddingTop: 8,
  paddingBottom: 0,
};

export const cardStyle: React.CSSProperties = {
  borderRadius,
  boxShadow,
};

export const dropdownListStyle: React.CSSProperties = {
  width: 200,
  paddingTop: 8,
  paddingBottom: 8,
  ...popoverStyle
};

export const gridStyle: React.CSSProperties = {
  borderTopWidth: 0,
  borderBottomWidth: 0,
  marginBottom: borderRadius,
  borderBottomLeftRadius: borderRadius,
  borderBottomRightRadius: borderRadius,
};

export const dividerBorder = `1px solid ${colors.dividerColor}`;
export const border = `1px solid ${colors.borderColor}`;
