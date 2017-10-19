import * as classNames from 'classnames';
import * as React from 'react';
import {Expandable} from '../../../../types/Types';
import {Icon} from '../../../common/components/icons/Icon';
import {Row} from '../../../common/components/layouts/row/Row';
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
      <div className={classNames('Row Row-center LinkItem')}>{name}</div>
      <Icon name="chevron-left" className="Row-right flex-1" size="small"/>
    </Row>
  );
};
