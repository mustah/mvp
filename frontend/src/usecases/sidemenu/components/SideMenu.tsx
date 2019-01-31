import Drawer from 'material-ui/Drawer';
import * as React from 'react';
import {mvpTheme, sideMenuWidth} from '../../../app/themes';
import {WithChildren} from '../../../types/Types';
import {StateToProps} from '../containers/SideMenuContainer';
import './SideMenu.scss';

const appBarHeight: number = mvpTheme.appBar!.height!;

const containerStyle: React.CSSProperties = {
  top: appBarHeight,
  paddingBottom: appBarHeight + 24,
};

export const SideMenu = ({isSideMenuOpen, children}: StateToProps & WithChildren) => (
  <Drawer
    containerClassName="DrawerContainer"
    open={isSideMenuOpen}
    docked={true}
    containerStyle={containerStyle}
    width={sideMenuWidth}
  >
    {children}
  </Drawer>
);
