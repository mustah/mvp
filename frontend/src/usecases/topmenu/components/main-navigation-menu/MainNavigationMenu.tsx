import * as classNames from 'classnames';
import * as React from 'react';
import {NavigationMenuIcon} from '../../../common/components/icons/NavigationMenuIcon';
import {Column} from '../../../common/components/layouts/column/Column';
import {MenuSeparator} from '../separators/MenuSeparator';
import './MainNavigationMenu.scss';

interface MainNavigationMenuProps {
  isOpen: boolean;
  disabled: boolean;
  toggleShowHideSideMenu: () => any;
}

export const MainNavigationMenu = (props: MainNavigationMenuProps) => {
  const {isOpen, toggleShowHideSideMenu, disabled} = props;
  return (
    <Column className={classNames('MainNavigationMenu Column-align-bottom', {isOpen})}>
      <NavigationMenuIcon color="black" onClick={toggleShowHideSideMenu} disabled={disabled}/>
      <MenuSeparator/>
    </Column>
  );
};
