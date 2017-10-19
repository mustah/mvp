import * as classNames from 'classnames';
import * as React from 'react';
import {Selectable} from '../../../../types/Types';
import {Icon} from '../../../common/components/icons/Icon';
import {ColumnCenter} from '../../../common/components/layouts/column/Column';
import {Xsmall} from '../../../common/components/texts/Texts';
import './MenuItem.scss';

export interface MenuItemProps extends Selectable {
  name: string;
  icon: string;
}

export const MenuItem = (props: MenuItemProps) => {
  const {name, icon, isSelected} = props;
  return (
    <ColumnCenter className={classNames('MenuItem', {isSelected})}>
      <Icon className="MenuItem-icon" name={icon}/>
      <Xsmall className="Bold">{name}</Xsmall>
    </ColumnCenter>
  );
};
