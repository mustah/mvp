import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import * as React from 'react';
import {connect} from 'react-redux';
import {ConnectedRouter} from 'react-router-redux';
import {PersistGate} from 'redux-persist/integration/react';
import {App} from './App';
import {history} from './routes';
import {Theme} from './themes';
import {withThemeProvider} from '../components/hoc/withThemeProvider';
import {LoadingLarge} from '../components/loading/Loading';
import {RootState} from '../reducers/rootReducer';
import {persistor} from '../store/configureStore';
import {getTheme} from '../usecases/theme/themeSelectors';

const ThemeProvider = withThemeProvider(() => (
  <PersistGate loading={<LoadingLarge/>} persistor={persistor}>
    <ConnectedRouter history={history}>
      <App/>
    </ConnectedRouter>
  </PersistGate>
));

const ThemeProviders = ({cssStyles, muiTheme}: Theme) => (
  <MuiThemeProvider muiTheme={muiTheme}>
    <ThemeProvider cssStyles={cssStyles}/>
  </MuiThemeProvider>
);

const mapStateToProps = (rootState: RootState): Theme => getTheme(rootState);

export const AppContainer = connect<Theme>(mapStateToProps)(ThemeProviders);
