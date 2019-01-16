import * as React from 'react';
import {Column} from '../../../../components/layouts/column/Column';
import {WithChildren} from '../../../../types/Types';
import './MainMenu.scss';

export const MainMenu = ({children}: WithChildren) => (
  <Column className="MainMenu MenuItems space-between">
    {children}
  </Column>
);
