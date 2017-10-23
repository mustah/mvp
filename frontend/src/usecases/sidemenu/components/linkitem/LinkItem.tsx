import * as classNames from 'classnames';
import * as React from 'react';
import {Expandable} from '../../../../types/Types';
import {IconRightArrow} from '../../../common/components/icons/IconRightArrow';
import {Row} from '../../../common/components/layouts/row/Row';
import './LinkItem.scss';

export interface LinkItemProps extends Expandable {
  icon?: string;
  name: string;
}

export const LinkItem = (props: LinkItemProps) => {
  const {name} = props;
  return (
    <Row>
      <div className={classNames('Row Row-center LinkItem')}>{name}</div>
      <IconRightArrow/>
    </Row>
  );
};
