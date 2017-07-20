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
  const {name} = props;
  /* TODO : add icon for showing if node is expanded or not*/
  return (
    <Column>
      <Row>
        <Icon/>
        <div className="LinkItem">{name}</div>
      </Row>
    </Column>
  );
};
