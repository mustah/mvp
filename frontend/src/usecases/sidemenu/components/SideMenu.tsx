import {default as classNames} from 'classnames';
import Drawer from 'material-ui/Drawer';
import * as React from 'react';
import {drawerContainerStyle, sideMenuWidth} from '../../../app/themes';
import {Footer} from '../../../components/footer/Footer';
import {StateToProps} from '../../../components/hoc/withSideMenu';
import {Column} from '../../../components/layouts/column/Column';
import {ClassNamed, WithChildren} from '../../../types/Types';
import './SideMenu.scss';

type Props = StateToProps & WithChildren & ClassNamed;

export const SideMenu = ({className, isSideMenuOpen, children}: Props) => (
  <Drawer
    className={classNames('SideMenu', className)}
    containerStyle={{...drawerContainerStyle, left: isSideMenuOpen ? 0 : 44, paddingBottom: 122}}
    docked={true}
    open={isSideMenuOpen}
    width={sideMenuWidth}
  >
    <Column className="space-between full-height scrollY">
      {children}
      <Footer/>
    </Column>
  </Drawer>
);
