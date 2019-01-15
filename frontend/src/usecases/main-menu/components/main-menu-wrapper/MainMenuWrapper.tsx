import * as React from 'react';
import {Column} from '../../../../components/layouts/column/Column';
import {WithChildren} from '../../../../types/Types';
import './MainMenuWrapper.scss';

export const MainMenuWrapper = ({children}: WithChildren) => (
  <Column className="MainMenuWrapper MenuItems space-between">
    {children}
  </Column>
);
