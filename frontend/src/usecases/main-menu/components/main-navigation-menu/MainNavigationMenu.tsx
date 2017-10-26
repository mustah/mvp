import * as React from 'react';
import {IconNavigationMenu} from '../../../common/components/icons/IconNavigationMenu';
import {RowCenter} from '../../../common/components/layouts/row/Row';
import './MainNavigationMenu.scss';

interface MainNavigationMenuProps {
  isOpen: boolean;
  toggleShowHideSideMenu: () => any;
}

export const MainNavigationMenu = (props: MainNavigationMenuProps) => {
  const {isOpen, toggleShowHideSideMenu} = props;
  return (
    <RowCenter className="MainNavigationMenu">
      <IconNavigationMenu
        color="white"
        onClick={toggleShowHideSideMenu}
        isOpen={isOpen}
      />
    </RowCenter>
  );
};
