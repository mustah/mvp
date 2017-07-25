import * as React from 'react';
import {Expandable} from '../../../types/Types';
import {Icon} from '../../common/components/icons/Icons';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import './LinkItem.scss';

export interface LinkItemProps extends Expandable {
  icon: string;
  name: string;
}

export const LinkItem = (props: LinkItemProps) => {
  const {icon, name} = props;
  return (
    <Row>
      <Icon name={icon} size="small"/>
      <Column className="Column-center">
        <div className="LinkItem">{name}</div>
      </Column>
    </Row>
  );
};
