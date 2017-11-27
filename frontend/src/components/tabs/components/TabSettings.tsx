import * as React from 'react';
import {translate} from '../../../services/translationService';
import {ActionsDropdown, menuItem, MenuItems} from '../../actions-dropdown/ActionsDropdown';
import {Column} from '../../layouts/column/Column';
import {TabUnderline} from './TabUnderliner';

export const TabSettings = () => {
  const noop = () => 0;

  const menuItems: MenuItems = [
    {name: translate('export to Excel (.csv)'), onClick: noop},
    {name: translate('export to JSON'), onClick: noop},
  ].map(menuItem);

  return (
    <Column className="TabSettings">
      <ActionsDropdown menuItems={menuItems} className="flex-1 Row-right"/>
      <TabUnderline/>
    </Column>
  );
};
