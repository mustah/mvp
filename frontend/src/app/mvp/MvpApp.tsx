import * as classNames from 'classnames';
import AppBar from 'material-ui/AppBar';
import 'MvpApp.scss';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {Layout} from '../../components/layouts/layout/Layout';
import {Row} from '../../components/layouts/row/Row';
import {MessageContainer} from '../../containers/message/MessageContainer';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {fetchGateways} from '../../state/domain-models/gateway/gatewayApiActions';
import {fetchAllMeters} from '../../state/domain-models/meter-all/allMetersApiActions';
import {
  getEncodedUriParametersForAllMeters,
  getEncodedUriParametersForGateways,
} from '../../state/search/selection/selectionSelectors';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {OnClick, Fetch} from '../../types/Types';
import {MainMenuToggleIcon} from '../../usecases/main-menu/components/menuitems/MainMenuToggleIcon';
import {MvpMainMenuContainer} from '../../usecases/main-menu/containers/MvpMainMenuContainer';
import {SavedSelectionsContainer} from '../../usecases/sidemenu/containers/savedSelections/SavedSelectionsContainer';
import {SelectionTreeContainer} from '../../usecases/sidemenu/containers/selection-tree/SelectionTreeContainer';
import {SideMenuContainer} from '../../usecases/sidemenu/containers/SideMenuContainer';
import {toggleShowHideSideMenu} from '../../usecases/sidemenu/sideMenuActions';
import {MvpPages} from './MvpPages';

interface StateToProps {
  isSideMenuOpen: boolean;
  encodedUriParametersForGateways: string;
  encodedUriParametersForAllMeters: string;
}

interface DispatchToProps {
  toggleShowHideSideMenu: OnClick;
  fetchGateways: Fetch;
  fetchAllMeters: Fetch;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class MvpApp extends React.Component<Props> {

  componentDidMount() {
    const {
      fetchGateways,
      encodedUriParametersForGateways,
      fetchAllMeters,
      encodedUriParametersForAllMeters,
    } = this.props;
    fetchGateways(encodedUriParametersForGateways);
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  componentWillReceiveProps(
    {
      fetchGateways,
      encodedUriParametersForGateways,
      fetchAllMeters,
      encodedUriParametersForAllMeters,
    }: Props) {
    fetchGateways(encodedUriParametersForGateways);
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  render() {
    const {isSideMenuOpen, toggleShowHideSideMenu} = this.props;

    return (
      <Row className="MvpApp">
        <MvpMainMenuContainer/>

        <Layout className={classNames('SideMenuContainer', {isSideMenuOpen})}>
          <SideMenuContainer>
            <AppBar
              className="AppTitle"
              title={translate('metering')}
              showMenuIconButton={false}
            />
            <SavedSelectionsContainer/>
            <SelectionTreeContainer topLevel={'cities'}/>
          </SideMenuContainer>
        </Layout>
        <MainMenuToggleIcon onClick={toggleShowHideSideMenu} isSideMenuOpen={isSideMenuOpen}/>

        <MvpPages/>
        <MessageContainer/>
      </Row>
    );
  }
}

const mapStateToProps = ({ui, searchParameters}: RootState): StateToProps => ({
  isSideMenuOpen: isSideMenuOpen(ui),
  encodedUriParametersForGateways: getEncodedUriParametersForGateways(searchParameters),
  encodedUriParametersForAllMeters: getEncodedUriParametersForAllMeters(searchParameters),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleShowHideSideMenu,
  fetchGateways,
  fetchAllMeters,
}, dispatch);

export const MvpAppContainer =
  connect<StateToProps, DispatchToProps, Props>(mapStateToProps, mapDispatchToProps)(MvpApp);
