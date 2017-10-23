import * as React from 'react';
import {IconNavigationMenu} from '../../../common/components/icons/IconNavigationMenu';
import {RowCenter} from '../../../common/components/layouts/row/Row';
import './MainNavigationMenu.scss';
import {Logo} from '../../../branding/components/Logo';

interface MainNavigationMenuProps {
  isOpen: boolean;
  disabled: boolean;
  toggleShowHideSideMenu: () => any;
}

export const MainNavigationMenu = (props: MainNavigationMenuProps) => {
  const {isOpen, toggleShowHideSideMenu, disabled} = props;
  return (
    <RowCenter className="MainNavigationMenu">
      <Logo fill="#fff"/>
      <IconNavigationMenu
        color="white"
        onClick={toggleShowHideSideMenu}
        disabled={disabled}
        isOpen={isOpen}
      />
    </RowCenter>
  );
};
