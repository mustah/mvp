import Drawer from 'material-ui/Drawer';
import * as React from 'react';
import {connect} from 'react-redux';
import {drawerWidth, mvpTheme} from '../../../app/themes';
import {RootState} from '../../../reducers/rootReducer';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {Children} from '../../../types/Types';
import './SideMenuContainer.scss';

interface StateToProps {
  isSideMenuOpen: boolean;
}

interface OwnProps {
  children?: Children;
}

const appBarHeight: number = mvpTheme.appBar!.height!;

const containerStyle: React.CSSProperties = {
  left: drawerWidth,
  top: appBarHeight,
  paddingBottom: appBarHeight + 24,
};

const SideMenu = ({isSideMenuOpen, children}: StateToProps & OwnProps) => (
  <Drawer
    containerClassName="DrawerContainer"
    open={isSideMenuOpen}
    docked={true}
    containerStyle={containerStyle}
  >
    {children}
  </Drawer>
);

const mapStateToProps = ({ui}: RootState): StateToProps => ({
  isSideMenuOpen: isSideMenuOpen(ui),
});

export const SideMenuContainer =
  connect<StateToProps, {}, OwnProps>(mapStateToProps)(SideMenu);
