import {darkBlack, fullBlack} from 'material-ui/styles/colors';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import * as React from 'react';

interface Styles {
  [key: string]: React.CSSProperties;
}

export const fontSizeNormal = 14;

const evoBorderRadius: React.CSSProperties = {borderRadius: 4};

export const colors = {
  darkBlue: '#006da3',
  darkGreen: '#4caf50',
  blue: '#00b6f7',
  orange: '#ff9800',
  red: '#ff3d00',
  white: '#ffffff',
  lightGrey: '#f9f9f9',
  black: '#000000',
  lightBlack: '#7b7b7b',
  borderColor: '#cccccc',
  dividerColor: '#eaeaea',
  iconHover: '#0f2228',
};

export const mvpTheme = getMuiTheme({
  appBar: {
    height: 60,
    padding: 16,
    color: colors.darkBlue,
  },
  fontFamily: 'TTNorms, Arial, sans-serif',
  palette: {
    primary1Color: fullBlack,
    textColor: darkBlack,
  },
  dialog: {
    bodyFontSize: fontSizeNormal,
    bodyColor: darkBlack,
  },
});

export const drawerWidth = 84; // Should be the same as $main-menu-width in _variables.scss

export const iconStyle: React.CSSProperties = {
  padding: 0,
  width: 30,
  height: 30,
};

export const iconSizeLarge: React.CSSProperties = {
  width: 28,
  height: 28,
};

export const sideBarStyles: Styles = {
  fontSize: {fontSize: fontSizeNormal},
  padding: {padding: '5px 0'},
  selected: {color: colors.blue},
  notSelectable: {color: colors.lightBlack},
  onHover: {color: colors.lightGrey},
};

export const sideBarHeaderStyle: React.CSSProperties = {
  fontWeight: 'bold',
  fontSize: fontSizeNormal,
  paddingTop: 0,
};

export const listStyle: React.CSSProperties = {
  padding: 0,
};

export const dividerStyle: React.CSSProperties = {
  backgroundColor: colors.dividerColor,
  marginTop: 10,
  marginBottom: 10,
};

export const listItemStyle: React.CSSProperties = {
  fontSize: fontSizeNormal,
  textStyle: {
    display: 'inline-block',
    textOverflow: 'ellipsis',
    maxWidth: 150,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    // instead of hardcoding a height, the padding alleviates the "inline-block"
    // that causes descenders (g j p etc) to be cut off (tested in Chrome and Firefox)
    padding: '1px 0',
  },
};

export const listItemStyleWithActions: React.CSSProperties = {
  ...listItemStyle,
  textStyle: {
    ...listItemStyle.textStyle,
    maxWidth: 100,
  },
};

export const listItemStyleSelected: React.CSSProperties = {
  ...listItemStyle,
  ...sideBarStyles.selected,
};

export const menuItemInnerDivStyle: React.CSSProperties = {
  ...listItemStyle,
  lineHeight: '32px',
  minHeight: 32,
};

export const nestedListItemStyle: React.CSSProperties = {
  paddingTop: 0,
  paddingBottom: 0,
};

export const dropDownStyle: Styles = {
  popoverStyle: {marginTop: 6, marginLeft: 2},
  listStyle: {outline: 'none', paddingLeft: 5, flex: 1},
  parentStyle: {fontSize: 11, fontWeight: 'normal', color: colors.lightBlack},
};

export const underlineFocusStyle = {
  borderColor: colors.blue,
};

export const floatingLabelFocusStyle = {
  color: colors.blue,
};

export const paperStyle: React.CSSProperties = {
  paddingTop: 16,
  paddingBottom: 16,
  boxShadow: '0 1px 3px rgba(0, 0, 0, 0.16)',
  ...evoBorderRadius,
};

export const mainContentPaperStyle: React.CSSProperties = {
  ...paperStyle,
  paddingBottom: 0,
};

export const cardStyle: React.CSSProperties = {
  ...evoBorderRadius,
};

export const buttonStyle: React.CSSProperties = {
  backgroundColor: colors.blue,
  color: colors.white,
};
