import AppBar from 'material-ui/AppBar';
import Drawer from 'material-ui/Drawer';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import 'SideMenuContainer.scss';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {OnClick} from '../../../types/Types';
import {drawerWidth} from '../../app/themes';
import {IconNavigationMenu} from '../../common/components/icons/IconNavigationMenu';
import {SavedSelectionsContainer} from '../components/savedSelections/SavedSelections';
import {toggleShowHideSideMenu} from '../sideMenuActions';
import {SelectionTreeContainer} from './selection-tree/SelectionTreeContainer';

interface StateToProps {
  isSideMenuOpen: boolean;
}

interface DispatchToProps {
  toggleShowHideSideMenu: OnClick;
}

const SideMenuContainerComponent = (props: StateToProps & DispatchToProps) => {
  const {isSideMenuOpen} = props;
  return (
    <Drawer
      containerClassName="DrawerContainer"
      open={isSideMenuOpen}
      docked={true}
      containerStyle={{left: isSideMenuOpen ? drawerWidth : 0}}
    >
      <AppBar
        className="AppTitle"
        title={translate('metering')}
        iconElementRight={<IconNavigationMenu onClick={props.toggleShowHideSideMenu}/>}
        showMenuIconButton={false}
      />
      <SavedSelectionsContainer/>

      <SelectionTreeContainer topLevel={'cities'} />
    </Drawer>
  );
};

const mapStateToProps = ({ui}: RootState): StateToProps => {
  return {
    isSideMenuOpen: isSideMenuOpen(ui),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleShowHideSideMenu,
}, dispatch);

export const SideMenuContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(SideMenuContainerComponent);
