import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {Layout} from '../../components/layouts/layout/Layout';
import {Row} from '../../components/layouts/row/Row';
import {RootState} from '../../reducers/rootReducer';
import {fetchGateways, fetchMeters} from '../../state/domain-models/domainModelsActions';
import {getEncodedUriParametersForMeters} from '../../state/search/selection/selectionSelectors';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {MainMenuContainer} from '../../usecases/main-menu/containers/MainMenuContainer';
import {SideMenuContainer} from '../../usecases/sidemenu/containers/SideMenuContainer';
import {MvpPages} from './MvpPages';
import * as classNames from 'classnames';
import './MvpApp.scss';

interface StateToProps {
  isSideMenuOpen: boolean;
  encodedUriParametersForMeters: string;
  encodedUriParametersForGateways: string;
}

interface DispatchToProps {
  fetchGateways: (encodedUriParameters: string) => void;
  fetchMeters: (encodedUriParameters: string) => void;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class MvpApp extends React.Component<Props> {

  componentDidMount() {
    const {fetchGateways, fetchMeters, encodedUriParametersForMeters, encodedUriParametersForGateways} = this.props;
    fetchGateways(encodedUriParametersForMeters);
    fetchMeters(encodedUriParametersForGateways);
  }

  render() {
    const {isSideMenuOpen} = this.props;

    return (
      <Row className="MvpApp">
        <MainMenuContainer/>

        <Layout className={classNames('SideMenuContainer', {isSideMenuOpen})}>
          <SideMenuContainer/>
        </Layout>

        <MvpPages/>
      </Row>
    );
  }
}

const mapStateToProps = ({ui, searchParameters}: RootState) => ({
  isSideMenuOpen: isSideMenuOpen(ui),
  encodedUriParametersForMeters: getEncodedUriParametersForMeters(searchParameters),
  encodedUriParametersForGateways: getEncodedUriParametersForMeters(searchParameters),
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchGateways,
  fetchMeters,
}, dispatch);

export const MvpAppContainer =
  connect<StateToProps, DispatchToProps, Props>(mapStateToProps, mapDispatchToProps)(MvpApp);
