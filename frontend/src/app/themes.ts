import {darkBlack, fullBlack} from 'material-ui/styles/colors';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import * as React from 'react';

interface Styles {
  [key: string]: React.CSSProperties;
}

export const fontSize = {
  small: 12,
  normal: 14,
  medium: 16,
};

export const borderRadius = 4;

export const boxShadow =
  '0px 1px 3px 0px rgba(0, 0, 0, 0.2), 0px 1px 1px 0px rgba(0, 0, 0, 0.14), 0px 2px 1px -1px rgba(0, 0, 0, 0.12)';

export const popoverBoxShadow =
  '0px 5px 5px -3px rgba(0,0,0,0.2), 0px 8px 10px 1px rgba(0,0,0,0.14), 0px 3px 14px 2px rgba(0,0,0,0.12)';

export const colors = {
  darkBlue: '#006da3',
  darkGreen: '#4caf50',
  blue: '#00b0ff',
  orange: '#ff9800',
  red: '#e84d6f',
  white: '#ffffff',
  lightGrey: '#f9f9f9',
  black: '#000000',
  lightBlack: '#7b7b7b',
  borderColor: '#cccccc',
  dividerColor: '#eaeaea',
  iconHover: '#0f2228',
  listItemHover: '#e6f8ff',
  selectedListItem: '#b3ebff',
};

export const secondaryBg = '#f9f9f9';
export const secondaryBgActive = '#b6e2cc';
export const secondaryBgHover = '#edf8f2';

export const secondaryFg = '#3c3c3c';
export const secondaryFgActive = '#245c40';
export const secondaryFgHover = '#0f2228';

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
    bodyFontSize: fontSize.normal,
    bodyColor: darkBlack,
  },
  flatButton: {
    primaryTextColor: colors.blue,
  },
  listItem: {
    nestedLevelDepth: 14,
  },
  menuItem: {
    hoverColor: colors.listItemHover,
    selectedTextColor: colors.blue,
  },
  raisedButton: {
    primaryColor: colors.blue,
  }
});

export const sideMenuWidth = 300;

export const iconStyle: React.CSSProperties = {
  padding: 0,
  width: 30,
  height: 30,
};

export const iconSizeLarge: React.CSSProperties = {
  width: 28,
  height: 28,
};

export const actionMenuItemIconStyle: React.CSSProperties = {
  height: 18,
  width: 17,
  padding: 0,
  margin: '7px 0 0 4px',
};

export const dividerStyle: React.CSSProperties = {
  backgroundColor: colors.dividerColor
};

export const selectedStyle: React.CSSProperties = {
  color: colors.black,
  fontWeight: 'bold',
  backgroundColor: colors.selectedListItem
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
  ...selectedStyle,
  backgroundColor: secondaryBgActive,
  color: secondaryFgActive
};

export const menuItemInnerDivStyle: React.CSSProperties = {
  ...menuItemStyle,
  lineHeight: '32px',
  minHeight: 32,
};

export const popoverStyle: React.CSSProperties = {
  borderRadius,
  boxShadow: popoverBoxShadow,
};

export const dropdownStyle: Styles = {
  popoverStyle: {marginTop: 6, marginLeft: 2, ...popoverStyle},
  listStyle: {outline: 'none', paddingLeft: 5, flex: 1},
  parentStyle: {fontSize: 11, fontWeight: 'normal', color: colors.lightBlack},
};

export const underlineFocusStyle = {
  borderColor: colors.blue,
  borderWidth: 2,
};

export const floatingLabelFocusStyle = {
  color: colors.blue,
};

export const paperStyle: React.CSSProperties = {
  paddingTop: 16,
  paddingBottom: 16,
  boxShadow,
  borderRadius,
};

export const mainContentPaperStyle: React.CSSProperties = {
  ...paperStyle,
  paddingBottom: 0,
};

export const cardStyle: React.CSSProperties = {
  borderRadius,
  boxShadow,
};

export const buttonStyle: React.CSSProperties = {
  backgroundColor: colors.blue,
  color: colors.white,
};

export const dropdownListStyle: React.CSSProperties = {
  width: 200,
  ...popoverStyle
};
