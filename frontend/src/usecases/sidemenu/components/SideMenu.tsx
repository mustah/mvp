import Drawer from 'material-ui/Drawer';
import * as React from 'react';
import {drawerContainerStyle, sideMenuWidth} from '../../../app/themes';
import {WithChildren} from '../../../types/Types';
import {StateToProps} from '../containers/SideMenuContainer';

export const SideMenu = ({isSideMenuOpen, children}: StateToProps & WithChildren) => (
  <Drawer
    containerStyle={drawerContainerStyle}
    docked={true}
    open={isSideMenuOpen}
    width={sideMenuWidth}
  >
    {children}
  </Drawer>
);
