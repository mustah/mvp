import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';
import {bindActionCreators} from 'redux';
import {RootState} from '../../reducers/rootReducer';
import {fetchGateways} from '../../state/domain-models/gateway/gatewayActions';
import {fetchMeters} from '../../state/domain-models/meter/meterActions';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {Layout} from '../common/components/layouts/layout/Layout';
import {Row} from '../common/components/layouts/row/Row';
import {MainMenuContainer} from '../main-menu/containers/MainMenuContainer';
import {SideMenuContainer} from '../sidemenu/containers/SideMenuContainer';
import './_common.scss';
import './App.scss';
import {Pages} from './Pages';

interface StateToProps {
  isAuthenticated: boolean;
  isSideMenuOpen: boolean;
}

interface AppProps extends StateToProps {
  fetchGateways: (filter: any) => any;
  fetchMeters: (filter: any) => any;
}

/**
 * The Application root component should extend React.Component in order
 * for HMR (hot module reloading) to work properly. Otherwise, prefer
 * functional components.
 */
class App extends React.Component<AppProps> {
  componentDidMount() {
    this.props.fetchGateways([]); // TODO: Add filter instead of empty array.
    this.props.fetchMeters([]);
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

const mapStateToProps = ({auth, ui}: RootState): StateToProps => {
  return {
    isAuthenticated: auth.isAuthenticated,
    isSideMenuOpen: isSideMenuOpen(ui),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchGateways,
  fetchMeters,
}, dispatch);

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(App));
