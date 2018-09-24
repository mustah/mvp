import * as classNames from 'classnames';
import 'MvpApp.scss';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {Layout} from '../../components/layouts/layout/Layout';
import {Row} from '../../components/layouts/row/Row';
import {MessageContainer} from '../../containers/message/MessageContainer';
import {RootState} from '../../reducers/rootReducer';
import {isSelectionPage} from '../../selectors/routerSelectors';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {OnClick} from '../../types/Types';
import {MainMenuToggleIcon} from '../../usecases/main-menu/components/menuitems/MainMenuToggleIcon';
import {MvpMainMenuContainer} from '../../usecases/main-menu/containers/MvpMainMenuContainer';
import {SavedSelectionsContainer} from '../../usecases/sidemenu/containers/savedSelections/SavedSelectionsContainer';
import {SelectionTreeContainer} from '../../usecases/sidemenu/containers/selection-tree/SelectionTreeContainer';
import {SideMenuContainer} from '../../usecases/sidemenu/containers/SideMenuContainer';
import {toggleShowHideSideMenu} from '../../usecases/sidemenu/sideMenuActions';
import {MvpPages} from './MvpPages';

interface StateToProps {
  isSideMenuOpen: boolean;
  isSelectionPage: boolean;
}

interface DispatchToProps {
  toggleShowHideSideMenu: OnClick;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

const MvpApp = ({isSideMenuOpen, isSelectionPage, toggleShowHideSideMenu}: Props) => (
  <Row className="MvpApp">
    <MvpMainMenuContainer/>

    <MainMenuToggleIcon onClick={toggleShowHideSideMenu} isSideMenuOpen={isSideMenuOpen}/>

    <Layout className={classNames('SideMenuContainer', {isSideMenuOpen})}>
      <SideMenuContainer>
        <SavedSelectionsContainer/>
        {!isSelectionPage && <SelectionTreeContainer/>}
      </SideMenuContainer>
    </Layout>

    <MvpPages/>

    <MessageContainer/>
  </Row>
);

const mapStateToProps = ({routing, ui}: RootState): StateToProps => ({
  isSideMenuOpen: isSideMenuOpen(ui),
  isSelectionPage: isSelectionPage(routing),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleShowHideSideMenu,
}, dispatch);

export const MvpAppContainer =
  connect<StateToProps, DispatchToProps, Props>(mapStateToProps, mapDispatchToProps)(MvpApp);
