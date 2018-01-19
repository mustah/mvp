import AppBar from 'material-ui/AppBar';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {Layout} from '../../components/layouts/layout/Layout';
import {Row} from '../../components/layouts/row/Row';
import {MessageContainer} from '../../containers/message/MessageContainer';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {fetchGateways, fetchMeters} from '../../state/domain-models/domainModelsActions';
import {getEncodedUriParametersForMeters} from '../../state/search/selection/selectionSelectors';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {OnClick} from '../../types/Types';
import {MainMenuToggleIcon} from '../../usecases/main-menu/components/menuitems/MainMenuToggleIcon';
import {MvpMainMenuContainer} from '../../usecases/main-menu/containers/MvpMainMenuContainer';
import {SavedSelectionsContainer} from '../../usecases/sidemenu/components/savedSelections/SavedSelections';
import {SelectionTreeContainer} from '../../usecases/sidemenu/containers/selection-tree/SelectionTreeContainer';
import {SideMenuContainer} from '../../usecases/sidemenu/containers/SideMenuContainer';
import {toggleShowHideSideMenu} from '../../usecases/sidemenu/sideMenuActions';
import {MvpPages} from './MvpPages';
import * as classNames from 'classnames';
import 'MvpApp.scss';

interface StateToProps {
  isSideMenuOpen: boolean;
  encodedUriParametersForMeters: string;
  encodedUriParametersForGateways: string;
}

interface DispatchToProps {
  fetchGateways: (encodedUriParameters: string) => void;
  fetchMeters: (encodedUriParameters: string) => void;
  toggleShowHideSideMenu: OnClick;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class MvpApp extends React.Component<Props> {

  componentDidMount() {
    const {fetchGateways, fetchMeters, encodedUriParametersForMeters, encodedUriParametersForGateways} = this.props;
    fetchGateways(encodedUriParametersForMeters);
    fetchMeters(encodedUriParametersForGateways);
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

const mapStateToProps = ({ui, searchParameters}: RootState) => ({
  isSideMenuOpen: isSideMenuOpen(ui),
  encodedUriParametersForMeters: getEncodedUriParametersForMeters(searchParameters),
  encodedUriParametersForGateways: getEncodedUriParametersForMeters(searchParameters),
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchGateways,
  fetchMeters,
  toggleShowHideSideMenu,
}, dispatch);

export const MvpAppContainer =
  connect<StateToProps, DispatchToProps, Props>(mapStateToProps, mapDispatchToProps)(MvpApp);
