import Drawer from 'material-ui/Drawer';
import * as React from 'react';
import {mvpTheme} from '../../../app/themes';
import {OwnProps, StateToProps} from '../containers/SideMenuContainer';
import './SideMenu.scss';

const appBarHeight: number = mvpTheme.appBar!.height!;

const style: React.CSSProperties = {
  top: appBarHeight,
  paddingBottom: appBarHeight + 24,
};

export const SideMenu = ({isSideMenuOpen, children, containerStyle}: StateToProps & OwnProps) => {
  const menuStyle: React.CSSProperties = {...style, ...containerStyle};
  return (
    <Drawer
      containerClassName="DrawerContainer"
      open={isSideMenuOpen}
      docked={true}
      containerStyle={menuStyle}
    >
      {children}
    </Drawer>
  );
};
