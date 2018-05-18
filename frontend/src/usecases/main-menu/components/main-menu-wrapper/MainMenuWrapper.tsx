import * as React from 'react';
import {AppSwitchDropdownContainer} from '../../../../components/actions-dropdown/AppSwitchDropdown';
import {Column, ColumnBottom} from '../../../../components/layouts/column/Column';
import {Children} from '../../../../types/Types';
import './MainMenuWrapper.scss';

interface Props {
  children?: Children;
}

export const MainMenuWrapper = ({children}: Props) => (
  <Column className="MainMenuWrapper">
    <Column className="MenuItems space-between">
      {children}
    </Column>
    <ColumnBottom className="flex-1">
      <AppSwitchDropdownContainer/>
    </ColumnBottom>
  </Column>
);
