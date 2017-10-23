import {darkBlack, fullBlack} from 'material-ui/styles/colors';
import getMuiTheme from 'material-ui/styles/getMuiTheme';

export const mvpTheme = getMuiTheme({
  appBar: {
    height: 60,
    color: '#006da3',
  },
  fontFamily: 'PT Sans, sans-serif',
  palette: {
    primary1Color: fullBlack,
    textColor: darkBlack,
  },
});

export const drawerWidth = 64;

export const iconSize = {
  large: {
    width: 28,
    height: 28,
  },
};
