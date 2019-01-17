import Drawer from 'material-ui/Drawer';
import * as React from 'react';
import {connect} from 'react-redux';
import {mvpTheme} from '../../../app/themes';
import {RootState} from '../../../reducers/rootReducer';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {Children} from '../../../types/Types';
import './SideMenuContainer.scss';

interface StateToProps {
  isSideMenuOpen: boolean;
}

interface OwnProps {
  children?: Children;
  containerStyle?: React.CSSProperties;
}

const appBarHeight: number = mvpTheme.appBar!.height!;

const style: React.CSSProperties = {
  top: appBarHeight,
  paddingBottom: appBarHeight + 24,
};

const SideMenu = ({isSideMenuOpen, children, containerStyle}: StateToProps & OwnProps) => {
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

const mapStateToProps = ({ui}: RootState): StateToProps => ({
  isSideMenuOpen: isSideMenuOpen(ui),
});

export const SideMenuContainer =
  connect<StateToProps, {}, OwnProps>(mapStateToProps)(SideMenu);
