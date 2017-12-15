import * as classNames from 'classnames';
import * as React from 'react';
import {Selectable} from '../../../../types/Types';
import {RowCenter} from '../../../../components/layouts/row/Row';
import {Xsmall} from '../../../../components/texts/Texts';
import './MenuItem.scss';

export interface MenuItemProps extends Selectable {
  name: string;
  icon: React.ReactElement<any>;
}

export const MenuItem = (props: MenuItemProps) => {
  const {name, icon, isSelected} = props;
  return (
    <RowCenter className={classNames('MenuItem', {isSelected})}>
      {icon}
      <Xsmall className="Bold">{name}</Xsmall>
    </RowCenter>
  );
};
