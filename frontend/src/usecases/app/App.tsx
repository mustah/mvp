import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';
import {bindActionCreators} from 'redux';
import {RootState} from '../../reducers/rootReducer';
import {fetchGateways} from '../../state/domain-models/gateway/gatewayActions';
import {fetchMeters} from '../../state/domain-models/meter/meterActions';
import {getEncodedUriParameters} from '../../state/search/selection/selectionSelectors';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {Layout} from '../common/components/layouts/layout/Layout';
import {Row} from '../common/components/layouts/row/Row';
import {MainMenuContainer} from '../main-menu/containers/MainMenuContainer';
import {SideMenuContainer} from '../sidemenu/containers/SideMenuContainer';
import './App.scss';
import {Pages} from './Pages';

interface StateToProps {
  isAuthenticated: boolean;
  isSideMenuOpen: boolean;
  encodedUriParameters: string;
}

interface DispatchToProps {
  fetchGateways: (encodedUriParameters: string) => void;
  fetchMeters: (encodedUriParameters: string) => void;
}

/**
 * The Application root component should extend React.Component in order
 * for HMR (hot module reloading) to work properly. Otherwise, prefer
 * functional components.
 */
class AppComponent extends React.Component<StateToProps & DispatchToProps> {

  componentDidMount() {
    const {fetchGateways, fetchMeters, encodedUriParameters} = this.props;
    fetchGateways(encodedUriParameters);
    fetchMeters(encodedUriParameters);
  }

  render() {
    const {isAuthenticated, isSideMenuOpen} = this.props;

    return (
      <Row className="App">
        <MainMenuContainer/>

        <Layout className={classNames('SideMenuContainer', {isSideMenuOpen})} hide={!isAuthenticated}>
          <SideMenuContainer/>
        </Layout>

        <Pages/>
      </Row>
    );
  }
}

const mapStateToProps = ({auth, ui, searchParameters}: RootState): StateToProps => {
  return {
    isAuthenticated: auth.isAuthenticated,
    isSideMenuOpen: isSideMenuOpen(ui),
    encodedUriParameters: getEncodedUriParameters(searchParameters),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchGateways,
  fetchMeters,
}, dispatch);

const AppContainer = connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(AppComponent);

export const App = withRouter(AppContainer);
