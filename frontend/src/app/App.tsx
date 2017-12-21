import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';
import {bindActionCreators} from 'redux';
import {RootState} from '../reducers/rootReducer';
import {fetchGateways, fetchMeters} from '../state/domain-models/domainModelsActions';
import {getEncodedUriParametersForMeters} from '../state/search/selection/selectionSelectors';
import {isSideMenuOpen} from '../state/ui/uiSelectors';
import {Layout} from '../components/layouts/layout/Layout';
import {Row} from '../components/layouts/row/Row';
import {MainMenuContainer} from '../usecases/main-menu/containers/MainMenuContainer';
import {SideMenuContainer} from '../usecases/sidemenu/containers/SideMenuContainer';
import './App.scss';
import {Pages} from './Pages';

interface StateToProps {
  isAuthenticated: boolean;
  isSideMenuOpen: boolean;
  encodedUriParametersForMeters: string;
  encodedUriParametersForGateways: string;
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
    const {fetchGateways, fetchMeters, encodedUriParametersForMeters, encodedUriParametersForGateways} = this.props;
    fetchGateways(encodedUriParametersForMeters);
    fetchMeters(encodedUriParametersForGateways);
  }

  render() {
    const {isAuthenticated, isSideMenuOpen} = this.props;

    const classes: string[] = ['App'];
    if (!isAuthenticated) {
      classes.push('FullPageLogin');
    }
    const outerClasses = classNames(classes);

    return (
      <Row className={outerClasses}>
        <MainMenuContainer/>

        <Layout className={classNames('SideMenuContainer', {isSideMenuOpen})} hide={!isAuthenticated}>
          <SideMenuContainer/>
        </Layout>

        <Pages/>
      </Row>
    );
  }
}

const mapStateToProps = ({auth: {isAuthenticated}, ui, searchParameters}: RootState): StateToProps => {
  return {
    isAuthenticated,
    isSideMenuOpen: isSideMenuOpen(ui),
    encodedUriParametersForMeters: getEncodedUriParametersForMeters(searchParameters),
    encodedUriParametersForGateways: getEncodedUriParametersForMeters(searchParameters),
  };
};

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchGateways,
  fetchMeters,
}, dispatch);

const AppContainer = connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(AppComponent);

export const App = withRouter(AppContainer);
