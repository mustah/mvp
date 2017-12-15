import * as React from 'react';
import {translate} from '../../../services/translationService';
import {ActionMenuItem} from '../../actions-dropdown/ActionMenuItem';
import {ActionsDropdown, MenuItems} from '../../actions-dropdown/ActionsDropdown';
import {Column} from '../../layouts/column/Column';
import {TabUnderline} from './TabUnderliner';

export const TabSettings = () => {
  const noop = () => 0;

  const menuItems: MenuItems = [
    {name: translate('export to Excel (.csv)'), onClick: noop},
    {name: translate('export to JSON'), onClick: noop},
  ].map(ActionMenuItem);

  return (
    <Column className="TabSettings">
      <ActionsDropdown menuItems={menuItems} className="flex-1 Row-right"/>
      <TabUnderline/>
    </Column>
  );
};
