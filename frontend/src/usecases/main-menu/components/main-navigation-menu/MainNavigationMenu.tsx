import * as React from 'react';
import {NavigationMenuIcon} from '../../../common/components/icons/NavigationMenuIcon';
import {RowCenter} from '../../../common/components/layouts/row/Row';
import './MainNavigationMenu.scss';

interface MainNavigationMenuProps {
  isOpen: boolean;
  disabled: boolean;
  toggleShowHideSideMenu: () => any;
}

export const MainNavigationMenu = (props: MainNavigationMenuProps) => {
  const {isOpen, toggleShowHideSideMenu, disabled} = props;
  return (
    <RowCenter className="MainNavigationMenu">
      <NavigationMenuIcon
        color="white"
        onClick={toggleShowHideSideMenu}
        disabled={disabled}
        isOpen={isOpen}
      />
    </RowCenter>
  );
};
