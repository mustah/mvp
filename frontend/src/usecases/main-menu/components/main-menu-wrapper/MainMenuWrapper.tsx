import * as React from 'react';
import {AppSwitchDropdown} from '../../../../components/actions-dropdown/AppSwitchDropdown';
import {Column, ColumnBottom} from '../../../../components/layouts/column/Column';
import {adminComponent} from '../../../../helpers/hoc';
import {Children} from '../../../../types/Types';
import './MainMenuWrapper.scss';

interface Props {
  children?: Children;
}

const AppSwitchDropdownComponent = adminComponent(AppSwitchDropdown);

export const MainMenuWrapper = ({children}: Props) => (
  <Column className="MainMenuWrapper">
    <Column className="MenuItems space-between">
      {children}
    </Column>
    <ColumnBottom className="flex-1">
      <AppSwitchDropdownComponent/>
    </ColumnBottom>
  </Column>
);
