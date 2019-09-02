import {default as classNames} from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {Row} from '../../components/layouts/row/Row';
import {MessageContainer} from '../../containers/MessageContainer';
import {RootState} from '../../reducers/rootReducer';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {OnClick} from '../../types/Types';
import {MainMenuToggleIcon} from '../../usecases/main-menu/components/menu-items/MainMenuToggleIcon';
import {OtcMainMenuItemsContainer} from '../../usecases/main-menu/containers/OtcMainMenuItemsContainer';
import {SideMenuContainer} from '../../usecases/sidemenu/containers/SideMenuContainer';
import {toggleShowHideSideMenu} from '../../usecases/sidemenu/sideMenuActions';
import {Colors} from '../../usecases/theme/themeModels';
import {OtcPages} from './OtcPages';

interface StateToProps {
  color: Colors;
  isSideMenuOpen: boolean;
}

interface DispatchToProps {
  toggleShowHideSideMenu: OnClick;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

const App = ({color: {primary, secondary}, isSideMenuOpen, toggleShowHideSideMenu}: Props) => (
  <Row key={`otc-app-${primary}-${secondary}`}>
    <SideMenuContainer className={classNames({isSideMenuOpen})}>
      <OtcMainMenuItemsContainer/>
    </SideMenuContainer>
    <MainMenuToggleIcon onClick={toggleShowHideSideMenu} isSideMenuOpen={isSideMenuOpen}/>
    <OtcPages/>
    <MessageContainer/>
  </Row>
);

const mapStateToProps = ({theme: {color}, ui}: RootState): StateToProps => ({
  color,
  isSideMenuOpen: isSideMenuOpen(ui),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleShowHideSideMenu,
}, dispatch);

export const OtcAppContainer = connect(mapStateToProps, mapDispatchToProps)(App);
