import {darkBlack, fullBlack} from 'material-ui/styles/colors';
import getMuiTheme from 'material-ui/styles/getMuiTheme';

export const mvpTheme = getMuiTheme({
  appBar: {
    height: 68,
    color: '#006da3',
  },
  palette: {
    primary1Color: fullBlack,
    textColor: darkBlack,
  },
});
