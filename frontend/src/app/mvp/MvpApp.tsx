import {default as classNames} from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {Row} from '../../components/layouts/row/Row';
import {MessageContainer} from '../../containers/MessageContainer';
import {RootState} from '../../reducers/rootReducer';
import {isReportPage} from '../../selectors/routerSelectors';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {OnClick} from '../../types/Types';
import {MainMenuToggleIcon} from '../../usecases/main-menu/components/menu-items/MainMenuToggleIcon';
import {MvpMainMenuItemsContainer} from '../../usecases/main-menu/containers/MvpMainMenuItemsContainer';
import {SideMenuContainer} from '../../usecases/sidemenu/containers/SideMenuContainer';
import {toggleShowHideSideMenu} from '../../usecases/sidemenu/sideMenuActions';
import {Colors} from '../../usecases/theme/themeModels';
import {MvpPages} from './MvpPages';

interface StateToProps {
  color: Colors;
  isSideMenuOpen: boolean;
  isReportPage: boolean;
}

interface DispatchToProps {
  toggleShowHideSideMenu: OnClick;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

const MvpApp = ({color: {primary, secondary}, isSideMenuOpen, toggleShowHideSideMenu}: Props) => (
  <Row key={`app-${primary}-${secondary}`}>
    <SideMenuContainer className={classNames({isSideMenuOpen})}>
      <MvpMainMenuItemsContainer/>
    </SideMenuContainer>
    <MainMenuToggleIcon onClick={toggleShowHideSideMenu} isSideMenuOpen={isSideMenuOpen}/>
    <MvpPages/>
    <MessageContainer/>
  </Row>
);

const mapStateToProps = ({routing, theme: {color}, ui}: RootState): StateToProps => ({
  color,
  isSideMenuOpen: isSideMenuOpen(ui),
  isReportPage: isReportPage(routing),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleShowHideSideMenu,
}, dispatch);

export const MvpAppContainer =
  connect<StateToProps, DispatchToProps, Props>(mapStateToProps, mapDispatchToProps)(MvpApp);
