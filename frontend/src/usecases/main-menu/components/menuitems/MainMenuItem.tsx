import {default as classNames} from 'classnames';
import * as React from 'react';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {Normal} from '../../../../components/texts/Texts';
import {Selectable} from '../../../../types/Types';
import './MainMenuItem.scss';

export interface MenuItemProps extends Selectable {
  name: string;
  icon: React.ReactElement<any>;
}

export const MainMenuItem = ({name, icon, isSelected}: MenuItemProps) => (
  <RowMiddle className={classNames('MainMenuItem', {isSelected})}>
    {icon}
    <Normal className="first-uppercase">{name}</Normal>
  </RowMiddle>
);
