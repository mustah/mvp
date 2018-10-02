import {default as classNames} from 'classnames';
import * as React from 'react';
import {ColumnCenter} from '../../../../components/layouts/column/Column';
import {Small} from '../../../../components/texts/Texts';
import {Selectable} from '../../../../types/Types';
import './MenuItem.scss';

export interface MenuItemProps extends Selectable {
  name: string;
  icon: React.ReactElement<any>;
}

export const MenuItem = ({name, icon, isSelected}: MenuItemProps) => (
  <ColumnCenter className={classNames('MenuItem', {isSelected})}>
    {icon}
    <Small className="first-uppercase">{name}</Small>
  </ColumnCenter>
);
