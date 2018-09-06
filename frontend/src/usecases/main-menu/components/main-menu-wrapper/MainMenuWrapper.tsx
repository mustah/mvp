import * as React from 'react';
import {AppSwitchDropdown} from '../../../../components/actions-dropdown/AppSwitchDropdown';
import {adminOnly} from '../../../../components/hoc/withRoles';
import {Column, ColumnBottom} from '../../../../components/layouts/column/Column';
import {Children} from '../../../../types/Types';
import {ProfileContainer} from '../../../topmenu/containers/ProfileContainer';
import './MainMenuWrapper.scss';

interface Props {
  children?: Children;
}

const AppSwitchDropdownComponent = adminOnly(AppSwitchDropdown);

export const MainMenuWrapper = ({children}: Props) => (
  <Column className="MainMenuWrapper">
    <Column className="MenuItems space-between">
      {children}
    </Column>
    <ColumnBottom className="flex-1">
      <ProfileContainer/>
      <AppSwitchDropdownComponent/>
    </ColumnBottom>
  </Column>
);
