import * as React from 'react';
import {Selectable} from '../../../../types/Types';
import {Icon} from '../../../common/components/icons/Icons';
import {Bold} from '../../../common/components/texts/Texts';
import {Column} from '../../../layouts/components/column/Column';
import {Row} from '../../../layouts/components/row/Row';
import {MenuSeparator} from '../separators/MenuSeparator';
import './MenuItem.scss';

export interface MenuItemProps extends Selectable {
  name: string;
  icon: string;
}

export const MenuItem = (props: MenuItemProps) => {
  const {name, icon, isSelected} = props;
  return (
    <Column>
      <Row className="MenuItem Row-center">
        <Icon className="MenuItem-icon" name={icon}/>
        <Bold className="MenuItem-name">{name}</Bold>
      </Row>
      <MenuSeparator isSelected={isSelected}/>
    </Column>
  );
};
